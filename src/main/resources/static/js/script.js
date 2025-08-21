// 后端接口前缀（根据实际环境调整）
const API_PREFIX = "/device/";
// 自动刷新间隔（毫秒）
const REFRESH_INTERVAL = 1000;
// 存储当前通道状态信息（用于离线判断）
let channelStatusMap = new Map();
// 缓存提示框元素，避免重复获取
const tipBox = document.getElementById("tipBox");
// 提示框定时器ID，用于清理重复定时器
let tipTimer = null;

// 页面加载初始化
window.onload = () => {
    queryAllStatus(); // 首次查询
    setInterval(queryAllStatus, REFRESH_INTERVAL); // 定时刷新
};

//显示提示信息
function showTip(text, color) {
    // 清理之前的定时器，避免多个提示冲突
    if (tipTimer) clearTimeout(tipTimer);
    tipBox.innerText = text;
    tipBox.style.color = color;
    tipBox.style.display = "block";
    // 3秒后隐藏
    tipTimer = setTimeout(() => tipBox.style.display = "none", 3000);
}

//检查通道号是否在1-12范围内
function checkChannelRange(channel) {
    // 统一在方法内完成字符串转数字
    const channelNum = parseInt(channel, 10);
    // 校验逻辑
    if (isNaN(channelNum) || channelNum < 1 || channelNum > 12) {
        showTip("通道号需为1-12之间的整数", "red");
        return false;
    }
    return true;
}

//检查通道是否在线
function checkChannelOnline(channelNum) {
    const isOnline = channelStatusMap.get(channelNum);

    if (!channelStatusMap.has(channelNum) || !isOnline) {
        showTip(`通道${channelNum}处于离线状态，无法操作`, "red");
        return false;
    }
    return true;
}



// 查询所有通道状态（核心表格渲染逻辑）
function queryAllStatus() {
    const resultDiv = document.getElementById("result");
    const lastRefreshSpan = document.getElementById("lastRefresh");
    const requestUrl = API_PREFIX + "getAllStatus";

    fetch(requestUrl)
        .then(response => {
            if (!response.ok) {
                throw new Error(`接口请求失败（状态码：${response.status}）`);
            }
            return response.json();
        })
        .then(data => {
            // 重置通道状态映射
            channelStatusMap.clear();
            // 处理错误响应
            if (data.length === 1 && data[0].success === false) {
                resultDiv.innerHTML = `<div class="error">查询失败：${data[0].errorMsg}</div>`;
                return;
            }

            // 生成表格（删除更新时间列）
            let tableHtml = `
          <table>
            <tr>
              <th>通道号</th>
              <th>电流</th>
              <th>电压</th>
              <th>输出状态</th>
              <th>使能状态</th>
              <th>设置电流</th>
            </tr>
        `;

            // 遍历通道数据
            data.forEach(channelData => {
                const channel = channelData.globalChannel || '离线';
                const isOnline = channel !== '离线';

                // 存储通道在线状态
                if (channelData.globalChannel) {
                    channelStatusMap.set(channelData.globalChannel, isOnline);
                }

                const setCurr = channelData.setCurr != null ? `${channelData.setCurr} mA` : '-';
                const activeCurr = channelData.activeCurr != null ? `${channelData.activeCurr} mA` : '-';
                const activeVolt = channelData.activeVolt != null ? channelData.activeVolt : '-';
                const isShuChu = channelData.isShuChu != null
                    ? (channelData.isShuChu === 1 ? '开启' : '关闭')
                    : '-';
                const isShiNeng = channelData.isShiNeng != null
                    ? (channelData.isShiNeng === 1 ? '开启' : '关闭')
                    : '-';

                tableHtml += `
            <tr>
              <td>${channel}</td>
              <td>${activeCurr}</td>
              <td>${activeVolt}</td>
              <td>${isShuChu}</td>
              <td>${isShiNeng}</td>
              <td>${setCurr}</td>
            </tr>
          `;
            });

            tableHtml += `</table>`;
            resultDiv.innerHTML = tableHtml;
            // 更新刷新时间
            lastRefreshSpan.textContent = new Date().toLocaleString();
        })
        .catch(error => {
            resultDiv.innerHTML = `<div class="error">查询失败：${error.message}</div>`;
        });
}

// 设置电流（增加0-6000范围校验）
function handleSetCurrent() {
    const channel = document.getElementById("setChannel").value;
    const currentVal = document.getElementById("setCurrentVal").value;

    //将通道号转为十进制数字
    const channelNum = parseInt(channel, 10);
    // 调用通道号范围检查方法
    if (!checkChannelRange(channelNum)) return;
    // 调用通道在线检查方法
    if (!checkChannelOnline(channelNum)) return;

    //电流校验
    if (currentVal < 0 || currentVal > 6000 || isNaN(currentVal)) {
        showTip("设置电流需为0-6000之间的有效数值", "red");
        return;
    }

    // 构造请求体
    const reqData = {
        channel: channelNum,
        enable: parseInt(currentVal) //将电流转为整数
    };

    fetch(API_PREFIX + "setCurrent", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(reqData)
    })
        .then(response => {
            // 关键：判断 HTTP 状态码是否成功
            if (!response.ok) {
                // 失败时解析错误信息并抛出
                return response.text().then(errorMsg => {
                    throw new Error(errorMsg); // 这里的 errorMsg 是后端返回的具体错误
                });
            }
            // 成功时返回响应内容
            return response.text();
        })
        .then(res => {

            // 成功提示
            showTip(`设置电流成功：${res}`, "green");
            queryAllStatus();

        })
        .catch(error => {
            // 失败提示（显示后端返回的错误信息）
            showTip(`设置电流失败：${error.message}`, "red");
        });
}


// 设置使能
function handleSetEnable() {
    const channel = document.getElementById("setChannel").value;
    const enableState = document.getElementById("setEnableState").value;

    //将通道号转为十进制数字
    const channelNum = parseInt(channel, 10);
    // 调用通道号范围检查方法
    if (!checkChannelRange(channelNum)) return;
    // 调用通道在线检查方法
    if (!checkChannelOnline(channelNum)) return;

    const reqData = {
        channel:channelNum,
        enable: parseInt(enableState)
    };

    fetch(API_PREFIX + "setEnable", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(reqData)
    })
        .then(response => {
            if (!response.ok) {
                return response.text().then(errorMsg=>{
                    throw new Error(errorMsg);
                });
            }
            return response.text();
        })
        .then(res => {
            showTip(`设置使能成功：${res}`, "green");
            queryAllStatus();
        })
        .catch(error => {
            showTip(`设置使能失败：${error.message}`, "red");
        });
}

// 设置输出
function handleSetOutput() {
    const channel = document.getElementById("setChannel").value;
    const outputState = document.getElementById("setOutputState").value;

    //将通道号转为十进制数字
    const channelNum = parseInt(channel, 10);
    // 调用通道号范围检查方法
    if (!checkChannelRange(channelNum)) return;
    // 调用通道在线检查方法
    if (!checkChannelOnline(channelNum)) return;

    const reqData = {
        channel: channelNum,
        enable: parseInt(outputState)
    };

    fetch(API_PREFIX + "setPut", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(reqData)
    })

        .then(response => {
            if (!response.ok) {
                return response.text().then(errorMsg=>{
                    throw new Error(errorMsg);
                });
            }
            return response.text();
        })
        .then(res => {
            showTip(`设置输出成功：${res}`, "green");
            queryAllStatus();
        })
        .catch(error => {
            showTip(`设置输出失败：${error.message}`, "red");
        });
}