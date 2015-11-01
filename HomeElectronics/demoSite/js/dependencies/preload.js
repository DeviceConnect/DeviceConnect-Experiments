/**
 preload.js
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

jQuery.preloadImages = function(list){
    for(var i = 0; i<list.length; i++){
        jQuery("<img>").attr("src", list[i]);
    }
};
((function(){

  function addDoubleImg(list, name){
    list.push(name+'.png');
    list.push(name+'_down.png');
  }

  function addImg(list, name, double){
    list.push('img/'+name+'.png');
    if(double){
      list.push('img/'+name+'_down.png');
    }
  }

  var list = [];
  for (var i = 1; i < 17; i++) {
    var sliced = ("0"+i).slice(-2);
    if(i < 5){
      addImg(list,'bt_TV_'+sliced+'_on', true);
      addImg(list,'bt_TV_'+sliced+'_off', true);
    } else {
      addImg(list,'bt_TV_'+sliced,true);
    }
    if(i < 10){
      addImg(list,'bt_'+sliced+'_on',true);
      addImg(list,'bt_'+sliced+'_off',true);
    } else if(i < 12){
      addImg(list,'bt_'+sliced);
    }
  }

  addImg(list,'bt_back',true);
  addImg(list,'bt_edit',true);
  addImg(list,'bt_graph',true);

  addImg(list,'bt_TV_power',true);

  addImg(list,'icon_01');
  addImg(list,'icon_02');
  addImg(list,'icon_03');
  addImg(list,'icon_04');

  addImg(list,'img_ch_01');
  addImg(list,'img_ch_02',true);
  addImg(list,'img_ch_03',true);

  addImg(list,'img_control_01');
  addImg(list,'img_control_02',true);
  addImg(list,'img_control_03',true);

  addImg(list,'img_edit');
  addImg(list,'img_graph');
  addImg(list,'img_temperature');

  addImg(list,'img_vol_01');
  addImg(list,'img_vol_02',true);
  addImg(list,'img_vol_03',true);

  addImg(list,'line');
  addImg(list,'option_button_off');
  addImg(list,'option_button_on');
  addImg(list,'point');

  $.preloadImages(list);
})());
