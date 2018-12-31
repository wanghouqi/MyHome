/**
 * 将Select转变为支持输入内容对Select的Option进行筛选的js
 * @author wanghq
 * @Date 2017年12月4日 18:38:29
 * var $select = $("select["+TriangleDefinition.HTML_DEF_ATTR_KEY_FILTER_SELECT+"]").filterSelect();
   $(selectDOMRouteProcess).filterSelectReloadOption();// 把selectDOMRouteProcess变为可filterSelect
 */

$(document).ready(function(){ 
	// 这段代码，是遍历所有拥有 TriangleDefinition.HTML_DEF_ATTR_KEY_FILTER_SELECT 属性的元素，并把他们变为可编辑 
	if($("select["+Definition.HTML_DEF_ATTR_KEY_FILTER_SELECT+"]").length > 0){
		$("select["+Definition.HTML_DEF_ATTR_KEY_FILTER_SELECT+"]").filterSelect();
	}
}); 

/**
 * 将Select转化为FilterSelect
 */
$.fn.filterSelect = (function(){
    return function(){
        var $body = $("body");
        this.each(function(i, v){
            var inputVal = "";
        	if($(v).parent().is("div") && $(v).parent().attr("class") == "m-input-select"){
        		return false;// 如果当前Select已经是FilterSelect,则退出.
        	}
            var $sel = $(v);// 得到Select的DOM
            var $div = $('<div class="m-input-select" style="width:'+$sel.width()+'"></div>');//生成覆盖层的DIV,宽度等于原Select宽度
            var $input = $("<input type='text' class='m-input' style='width:"+$sel.width()+"' />");
            // var $wrapper = $("<div class='m-list-wrapper'><ul class='m-list'></ul></div>");
            var $wrapper = $("<ul class='m-list'></ul>");
            $div = $sel.wrap($div).hide().addClass("m-select").parent();
            $div.append($input).append("<span class='m-input-ico'></span>").append($wrapper);

            // 遮罩层显示 + 隐藏
            var wrapper = {
                show: function(){
                    $wrapper.show();
                    this.$list = $wrapper.find(".m-list-item:visible");
                    this.setIndex(this.$list.filter(".m-list-item-active"));
                    this.setActive(this.index);
                },
                hide: function(){
                    $wrapper.hide();
                },
                next: function(){
                    return this.setActive(this.index + 1);
                },
                prev: function(){
                    return this.setActive(this.index - 1);
                },
                $list: $wrapper.find(".m-list-item"),
                index: 0,
                $cur: [],
                setActive: function(i){
                    // 找到第1个 li，并且赋值为 active
                    var $list = this.$list, size = $list.length;
                    if(size <= 0){
                        this.$cur = [];
                        return;
                    }
                    $list.filter(".m-list-item-active").removeClass("m-list-item-active");
                    if(i < 0){
                        i = 0;
                    }else if(i >= size){
                        i = size - 1;
                    }
                    this.index = i;
                    this.$cur = $list.eq(i).addClass("m-list-item-active");
                    this.fixScroll(this.$cur);
                    return this.$cur;
                },
                fixScroll: function($elem){
                    // console.log($wrapper);
                    var height = $wrapper.height(), top = $elem.position().top, eHeight = $elem.outerHeight();
                    var scroll = $wrapper.scrollTop();
                    // 因为 li 的 实际　top，应该要加上 滚上 的距离
                    top += scroll;
                    if(scroll > top){
                        $wrapper.scrollTop(top);
                    }else if(top + eHeight > scroll + height){
                        // $wrapper.scrollTop(top + height - eHeight);
                        $wrapper.scrollTop(top + eHeight - height);
                    }
                },
                setIndex: function($li){
                    if($li.length > 0){
                        this.index = this.$list.index($li);
                        $li.addClass("m-list-item-active").siblings().removeClass("m-list-item-active");
                    }else{
                        this.index = 0;
                    }
                }
            };

            // input 的操作
            var operation = {
                // 文字更变了，更新 li, 最低效率的一种
                textChange: function(){
                    val = $.trim($input.val());
                    $wrapper.find(".m-list-item").each(function(i, v){
                        if(v.innerHTML.indexOf(val) >= 0){
                            $(v).show();
                        }else{
                            $(v).hide();
                        }
                    });
                    wrapper.show();
                },
                // 设值
                setValue: function($li){
                    if($li && $li.length > 0){
                        var val = $.trim($li.html());
                        $input.val(val).attr("placeholder", val);
                        $input.attr("title", val);
                        wrapper.setIndex($li);
                        $sel.val($li.attr("data-value")).trigger("change");
                    }else{
                        $input.val(function(i, v){
                            return $input.attr("placeholder");
                        });
                    };
                    wrapper.hide();
                    this.offBody();
                },
                onBody: function(){
                    var self = this;
                    setTimeout(function(){
                        self.offBody();
                        $body.on("click", self.bodyClick);
                    }, 10);
                },
                offBody: function(){
                    $body.off("click", this.bodyClick);
                },
                bodyClick: function(e){
                    var target = e.target;
                    if(target != $input[0] && target != $wrapper[0]){
                        wrapper.hide();
                        operation.setValue();
                        operation.offBody();
                    }
                }
            };

            // 遍历 $sel 对象
            function resetOption(){
                var html = "", val = "", defVal = "";
                $sel.find("option").each(function(i, v){
	               	if(i == 0){
	            		defVal = v.text;
	            	}
                    if(v.selected){
                        val = v.text;
                    };
                    html += '<li class="m-list-item'+ (v.selected ? " m-list-item-active" : "") +'" data-value="'+ v.value +'" title="'+ v.text +'">'+ v.text +'</li>';
                });
                if(val.length == 0 ){
               	 val = defVal;
                }
                $input.val(val);
                $input.attr("title", val);
                $input.attr("placeholder", val);
                $wrapper.html(html);
            };
            $sel.on("optionChange", resetOption).trigger("optionChange");
            $sel.on("setEditSelectValue", function(e, val){
                // console.log(val);
                var $all = $wrapper.find(".m-list-item"), $item;
                for(var i = 0, max = $all.size(); i < max; i++){
                    $item = $all.eq(i);
                    if($item.attr("data-value") == val){
                        operation.setValue($item);
                        return;
                    }
                }
            });

            // input 聚焦
            $input.on("focus", function(){
                this.value = "";
                operation.textChange();
                operation.onBody();
            }).on("input propertychange", function(e){
                operation.textChange();
            }).on("keydown", function(e){
                // 上 38, 下 40， enter 13
                switch(e.keyCode){
                    case 38:
                        wrapper.prev();
                        break;
                    case 40:
                        wrapper.next();
                        break;
                    case 13:
                        operation.setValue(wrapper.$cur);
                        break;
                }
            });

            $div.on("click", ".m-input-ico", function(){
                // 触发 focus 和 blur 事件
                // focus 是因为 input 有绑定
                // 而 blur，实际只是失去焦点而已，真正隐藏 wrapper 的是 $body 事件
                $wrapper.is(":visible") ? $input.blur() : ($input.val("").trigger("focus"));
            });

            // 选中
            $wrapper.on("click", ".m-list-item", function(){
                operation.setValue($(this));
                return false;
            });

            setTimeout(function(){
                // for ie
                wrapper.hide();
            }, 1)
            //将wrapper对象绑定到当前select上
            $(v).data("wrapper", wrapper);
        });
        return this;
    };
})();

/**
 * 更新当前FilterSelect的Option
 */
$.fn.filterSelectReloadOption = (function(){
    return function(keepInputValue){
        keepInputValue = keepInputValue || false;
        this.each(function(i, v){
        	var $div = $(v).parent();
        	if($div.is("div") && $div.attr("class") == "m-input-select"){
        		 var $sel = $(v);// 得到Select的DOM
                 var $input = $div.find(":text[class='m-input']");
                 var $wrapper = $div.find("ul[class='m-list']");
                 $wrapper.empty();
             	 var html = "", val = "", defVal = "";
                 $sel.find("option").each(function(i, v){
                	 if(i == 0){
                		 defVal = v.text;
                	 }
                     if(v.selected){
                         val = v.text;
                     };
                     html += '<li class="m-list-item'+ (v.selected ? " m-list-item-active" : "") +'" data-value="'+ v.value +'" title="'+ v.text +'">'+ v.text +'</li>';
                 });
                 if(val.length == 0 ){
                	 val = defVal;
                 }
                if (!keepInputValue) {
                    $input.val(val);
                    $input.attr("title", val);
                    $input.attr("placeholder", val);
                }
                 $wrapper.html(html);
                //获取绑定到当前select上的wrapper对象
                var wrapper = $sel.data("wrapper");
                //更新wrapper对象
                wrapper.$list = $wrapper.find(".m-list-item");
                wrapper.index = 0;
                wrapper.$cur = [];
            }
        });
        return this;
    };
})();