import React, { useState, useEffect } from 'react';
import './StatCard.css';
import { Bubble } from 'nivo';
import '../pages/ProfilePage.css';

function StatCard(props) {
  const stockNameToColorDict = {
    nvda: 'lime',
    amd: 'red',
    cash: 'green',
    baba: '#ff6a00',
    googl: '#F01716',
    msft: '#00A4EF',
    aapl: '#555555',
    lyft: '#FF00BF',
    fb: '#4267B2',
    mdb: '#3FA037',
    pton: 'black',
    save: 'yellow',
    ba: '#1A409F',
    tsla: '#cc0000'
  }

  useEffect(
    () => {

    }
  );

  function getStockColor(holding) {
    return (holding.id.toLowerCase() in stockNameToColorDict)?stockNameToColorDict[holding.id.toLowerCase()]:'white';
  }
  function getStockTextColor(backgroundColor) {
    // console.log(backgroundColor.colorR,backgroundColor.colorG, backgroundColor.colorB);
    if(backgroundColor.colorR===255&&backgroundColor.colorG===255&&backgroundColor.colorB===255) return 'black';
    else if(backgroundColor.colorR===0&&backgroundColor.colorG===0&&backgroundColor.colorB===0) return 'white';

    let hexColor = rgbToHex(backgroundColor.colorR,backgroundColor.colorG,backgroundColor.colorB)
    let newLabelColor = hexToComplimentary(hexColor);
    if(newLabelColor===hexColor) return 'black';
    else return newLabelColor;
  }
  function componentToHex(c) {
    var hex = c.toString(16);
    return hex.length === 1 ? "0" + hex : hex;
  }
  function rgbToHex(r, g, b) {
    return "#" + componentToHex(r) + componentToHex(g) + componentToHex(b);
  }
  function hexToComplimentary(hex){

      // Convert hex to rgb
      // Credit to Denis http://stackoverflow.com/a/36253499/4939630
      var rgb = 'rgb(' + (hex = hex.replace('#', '')).match(new RegExp('(.{' + hex.length/3 + '})', 'g')).map(function(l) { return parseInt(hex.length%2 ? l+l : l, 16); }).join(',') + ')';

      // Get array of RGB values
      rgb = rgb.replace(/[^\d,]/g, '').split(',');

      var r = rgb[0], g = rgb[1], b = rgb[2];

      // Convert RGB to HSL
      // Adapted from answer by 0x000f http://stackoverflow.com/a/34946092/4939630
      r /= 255.0;
      g /= 255.0;
      b /= 255.0;
      var max = Math.max(r, g, b);
      var min = Math.min(r, g, b);
      var h, s, l = (max + min) / 2.0;

      if(max === min) {
          h = s = 0;  //achromatic
      } else {
          var d = max - min;
          s = (l > 0.5 ? d / (2.0 - max - min) : d / (max + min));

          if(max === r && g >= b) {
              h = 1.0472 * (g - b) / d ;
          } else if(max === r && g < b) {
              h = 1.0472 * (g - b) / d + 6.2832;
          } else if(max === g) {
              h = 1.0472 * (b - r) / d + 2.0944;
          } else if(max === b) {
              h = 1.0472 * (r - g) / d + 4.1888;
          }
      }

      h = h / 6.2832 * 360.0 + 0;

      // Shift hue to opposite side of wheel and convert to [0-1] value
      h+= 180;
      if (h > 360) { h -= 360; }
      h /= 360;

      // Convert h s and l values into r g and b values
      // Adapted from answer by Mohsen http://stackoverflow.com/a/9493060/4939630
      if(s === 0){
          r = g = b = l; // achromatic
      } else {
          var hue2rgb = function hue2rgb(p, q, t){
              if(t < 0) t += 1;
              if(t > 1) t -= 1;
              if(t < 1/6) return p + (q - p) * 6 * t;
              if(t < 1/2) return q;
              if(t < 2/3) return p + (q - p) * (2/3 - t) * 6;
              return p;
          };

          var q = l < 0.5 ? l * (1 + s) : l + s - l * s;
          var p = 2 * l - q;

          r = hue2rgb(p, q, h + 1/3);
          g = hue2rgb(p, q, h);
          b = hue2rgb(p, q, h - 1/3);
      }

      r = Math.round(r * 255);
      g = Math.round(g * 255);
      b = Math.round(b * 255);

      // Convert r b and g values to hex
      rgb = b | (g << 8) | (r << 16);
      return "#" + (0x1000000 | rgb).toString(16).substring(1);
  }

  return (
    <div className="statistics-container">
        {/*Overall*/}
        {props.status==='Session Expired'&&<h6 className='inactive-header'>{'🤡Account Inactive🤡'}</h6>}
        {props.broker==='robinhood'&&<h5 className='robinhood-header'>{props.broker}</h5>}
        {props.broker==='webull'&&<h5 className='webull-header'>{props.broker}</h5>}
        <div className="overall-text">
            {
                props.overallChange === 'unavailable' || props.overallChange === 'n/a' ?
                    <div>
                        Overall: {props.overallChange}
                    </div>
                    :
                    <div>
                        {props.overallChange[0] === '+' ?
                            <div style={{ color: 'lime' }} >Overall: {props.overallChange}</div>
                            :
                            <div style={{ color: 'red' }} >Overall: {props.overallChange}</div>
                        }
                    </div>
            }
        </div>
        {/*Daily Weekly Monthly*/}
        <div className="dwm-text">
            {
                props.dailyChange === 'unavailable' || props.dailyChange === 'n/a' ?
                    <div className="stats-change">
                        <div>Daily: {props.dailyChange}</div>
                    </div>
                    :
                    <div className="stats-change">
                        {props.dailyChange[0] === '+' ?
                            <div style={{ color: 'lime' }} >Daily: {props.dailyChange}</div>
                            :
                            <div style={{ color: 'red' }} >Daily: {props.dailyChange}</div>
                        }
                    </div>
            }
            {
                props.weeklyChange === 'unavailable' || props.weeklyChange === 'n/a' ?
                    <div className="stats-change">
                        Weekly: {props.weeklyChange}
                    </div>
                    :
                    <div className="stats-change">
                        {props.weeklyChange[0] === '+' ?
                            <div style={{ color: 'lime' }} >Weekly: {props.weeklyChange}</div>
                            :
                            <div style={{ color: 'red' }} >Weekly: {props.weeklyChange}</div>
                        }
                    </div>
            }
            {
                props.monthlyChange === 'unavailable' || props.monthlyChange === 'n/a' ?
                    <div className="stats-change">
                        Monthly: {props.monthlyChange}
                    </div>
                    :
                    <div className="stats-change">
                        {props.monthlyChange[0] === '+' ?
                            <div style={{ color: 'lime' }} >Monthly: {props.monthlyChange}</div>
                            :
                            <div style={{ color: 'red' }} >Monthly: {props.monthlyChange}</div>
                        }
                    </div>
            }
        </div>
        <div className="bubble-chart" style={{marginTop:'20px'}}>
          <h2 className="bubble-chart-title">
            Positions
          </h2>
            {(props.sampleData.children.length==0)?(<h4 className="bubble-unavailable-text">Positions Unavailable</h4>)
            :
            (<Bubble
            root={props.sampleData}
            className='yes'
            width={320}
            height={300}
            leavesOnly={true}
            labelTextColor={getStockTextColor}
            colorBy={getStockColor}
            isInteractive={false}
            margin={{top: 10}}
          />)}
        </div>
    </div>
  );
}
export default StatCard;
