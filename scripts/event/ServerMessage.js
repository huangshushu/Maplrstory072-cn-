
var Message = new Array(
        "请不要使用非法程序，不然将受到严厉制裁。",
        "如果不能攻击或不能跟npc对话,请在聊天框打 @解卡/@ea 来解除异常状态.");

var setupTask;

function init() {
    scheduleNew();
}

function scheduleNew() {
    setupTask = em.schedule("start", 900000);
}

function cancelSchedule() {
    setupTask.cancel(false);
}

function start() {
    scheduleNew();
    em.broadcastYellowMsg("[冒险岛ONLINE 帮助]  " + Message[Math.floor(Math.random() * Message.length)]);
}