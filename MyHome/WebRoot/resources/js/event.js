/**
 * 防抖函数
 * @param method
 * @param context 方法运行时的this, arg调用方法的入参, delay去抖超时毫秒计
 */
function debounce(method, context, arg, delay) {
    clearTimeout(method.tId);
    method.tId = setTimeout(function() {
        method.call(context, arg);
    }, delay);
}