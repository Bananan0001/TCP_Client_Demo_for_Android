package com.example.tcp_v100

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import com.example.tcp_v100.R.xml.activity_mian
import java.io.InputStream
import java.io.OutputStream
import java.io.PrintWriter
import java.net.Socket
import java.net.UnknownHostException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.Executors

private var socket = Socket()
var serverAddress = "127.0.0.1"             // 默认服务器的 IP 地址
var serverPort = 123                        //默认服务器的端口号
private var ONandOFF = 1                //数据读取开关
private var indeta = 1
private var RG1 = 1
private var RG2 = 1
var Logdata: String=""

@RequiresApi(Build.VERSION_CODES.O)
class MainActivity : ComponentActivity() {
    private lateinit var Button1: Button
    private lateinit var Button2: Button
    private lateinit var Button3: Button
    private lateinit var btn1: Button
    private lateinit var btn2: Button
    private lateinit var btn3: Button
    private lateinit var btn4: Button
    private lateinit var radioGroup: RadioGroup
    private lateinit var radioGroup1: RadioGroup
    private lateinit var TextView1: TextView
    private lateinit var EditText1: EditText
    private lateinit var EditText2: EditText
    private lateinit var EditText3: EditText
    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activity_mian)
        Button1 = findViewById(R.id.Button1)
        Button2 = findViewById(R.id.Button2)
        Button3 = findViewById(R.id.Button3)
        btn1 = findViewById(R.id.btn1)
        btn2 = findViewById(R.id.btn2)
        btn3 = findViewById(R.id.btn3)
        btn4 = findViewById(R.id.btn4)
        radioGroup = findViewById(R.id.radioGroup)
        radioGroup1 = findViewById(R.id.radioGroup1)
        TextView1 = findViewById(R.id.TextView1)
        EditText1 = findViewById(R.id.EditText1)
        EditText2 = findViewById(R.id.EditText2)
        EditText3 = findViewById(R.id.EditText3)

        radioGroup.setOnCheckedChangeListener { radioGroup, checkedId ->
            val radioButton = findViewById<View>(checkedId) as RadioButton
            if (radioButton.getText() == "Hex"){
                RG1 = 1
                Log.e("debugLog", "选择了Hex发送")
            }else{
                RG1 = 2
                Log.e("debugLog", "选择了字符串发送")
            }
        }

        radioGroup1.setOnCheckedChangeListener { radioGroup, checkedId ->
            val radioButton = findViewById<View>(checkedId) as RadioButton
            if (radioButton.getText() == "Hex"){
                RG2 = 1
                Log.e("debugLog", "选择了Hex显示")
            }else{
                RG2 = 2
                Log.e("debugLog", "选择了字符串显示")
            }
        }

        RXD()//开启接收线程

        Button1.setOnClickListener {//监听链接按钮
            connect()
        }

        Button2.setOnClickListener {//监听断开按钮
            disconnect()
        }

        Button3.setOnClickListener {//监听发送按钮
            TXD()
        }
    }

    private fun connect(): Unit {//开始建立TCP链接
        Log.e("debugLog", "开始建立TCP链接")
        Thread {
            try {
                if(EditText1.getText()!=null&&EditText2.getText()!=null){           //检查有没有输入
                    serverAddress = EditText1.getText().toString()                  //读取服务器地址
                    serverPort = EditText2.getText().toString().toIntOrNull()!!     //读取端口
                }
                socket = Socket(serverAddress, serverPort)                          //开启链接
                Logdata = "链接成功：" +"地址："+serverAddress+"端口："+serverPort+"\n"+Logdata
                runOnUiThread {
                    TextView1.text = Logdata                                        //显示接入的地址和端口
                }
                yanchi(100)
                Log.e("debugLog", "建立TCP链接成功！")
                ONandOFF=0                                                           //开启数据接收
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("debugLog", "建立TCP链接失败！")
                Logdata = "链接失败：" + e.message+"   地址："+serverAddress+"   端口："+serverPort+"\n"+Logdata
                runOnUiThread {
                    TextView1.text = Logdata
                }
            }
        }.start()
    }

    private fun RXD(){//开启接收数据线程
        var msg = Array<Byte>(128){0}.toByteArray()
        var msg_len = 0
        Thread {
            Log.e("debugLog", "开启数据接收")
            while(indeta==1){
                try {
                    if(ONandOFF==0){
                        var reader = socket.getInputStream()
                        if (reader != null) {
                            msg_len = reader.read(msg)
                        }
                        var neomsg = Array<Byte>(msg_len){ 0 }.toByteArray()
                        for (i in 0 until msg_len){
                            neomsg[i]=msg[i]
                        }
                        if (RG2 == 1){
                            Logdata = Time()+"收--->"+neomsg.joinToString(" "){ "%02x".format(it) }+"\n"+Logdata
                            Log.e("debugLog", "数据接收成功(Hex)："+neomsg.joinToString(" "){ "%02x".format(it) })
                        }else {
                            var deta = String(msg,0,msg_len)
                            Log.e("debugLog", "数据接收成功(字符串)："+deta)
                            Logdata = Time()+"收--->"+deta+"\n"+Logdata
                        }
                        runOnUiThread {
                            TextView1.text = Logdata
                        }
                    }
                } catch (e: UnknownHostException){
                    e.printStackTrace()
                }catch (e: Exception) {
                    e.printStackTrace()
                    if(ONandOFF==0){
                        Logdata = "开启接收失败,请重新链接"+"\n"+Logdata
                        Log.e("debugLog", "开启接收失败："+e.message)
                    }
                    runOnUiThread {
                        TextView1.text =Logdata
                    }
                    ONandOFF=1
                }
                yanchi(1)
            }
        }.start()
    }

    private fun disconnect(){//关闭链接
        Log.e("debugLog", "断开链接")
        Thread {
            try {
                ONandOFF = 1     //关闭数据读取
                yanchi(10)
                socket.close()
                Logdata = "断开链接成功\n$Logdata"
                runOnUiThread {
                    TextView1.text = Logdata
                }
                Log.e("debugLog", "断开链接成功")
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    ONandOFF = 0     //恢复数据读取
                    Log.e("debugLog", "断开链接失败")
                    Logdata = "断开链接失败：" + e.message+"\n"+Logdata
                    runOnUiThread {
                        TextView1.text = Logdata
                    }
                }
            }
            Thread.sleep(100)
        }.start()
    }

    private fun TXD(){// 发送数据
        Thread {
            try {
                val outputStream: OutputStream = socket.getOutputStream()
                val out = PrintWriter(outputStream, true)
                var msg = Array<Byte>(128){ 0 }.toByteArray()
                val string1 = EditText3.getText()
                if (RG1 == 1){
                    var int: Int = 0
                    var int1: Int = 0
                    var int2: Char = '0'
                    var int3: Int = 0

                    val byteArray = string1.toString().toCharArray()
                    for (i in 0 until  byteArray.size){
                        if (byteArray[i] in '0'..'9'){
                            int2 = byteArray[i]-48
                            int1++
                        }
                        if (byteArray[i] in 'A'..'Z'){
                            int2 = byteArray[i]-55
                            int1++
                        }
                        if (byteArray[i] in 'a'..'z'){
                            int2 = byteArray[i]-87
                            int1++
                        }
                        if (int1 == 1){
                            int3 = int2.toInt().shl(4)
                        }
                        if (int1 == 2){
                            msg[int++] = (int3 or int2.code).toByte()
                            int1=0
                        }
                    }
                    var neomsg = Array<Byte>(int){ 0 }.toByteArray()
                    for (i in 0 until int){
                        neomsg[i]=msg[i]
                    }
                    outputStream.write(neomsg)
                    outputStream.flush() // 确保数据发送
                    Logdata = Time()+"发<---"+neomsg.joinToString(" "){ "%02x".format(it) }+"\n"+Logdata
                    Log.e("debugLog", "数据发送成功(Hex)："+neomsg.joinToString(" "){ "%02x".format(it) })
                }else {
                    out.println(string1)
                    out.flush()
                    Log.e("debugLog", "数据发送成功(字符串)："+string1)
                    Logdata = Time()+"发<---"+string1+"\n"+Logdata
                }
                runOnUiThread {
                    TextView1.text = Logdata//显示发送数据
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Logdata = "传输失败：" + e.message+"\n"+Logdata
                    runOnUiThread {
                        TextView1.text = Logdata
                    }
                }
            }
        }.start()
    }

    private fun Time(): String? {//获得系统时间并转换格式
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("dd HH:mm:ss")
        return currentDateTime.format(formatter)
    }

    private fun yanchi(time: Int) {//延时函数
        try {
            Thread.sleep(time.toLong())
        } catch (e: InterruptedException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }
    }

    fun stringToHexArray(input: String): ByteArray {
        return input.map { char ->
            char.toInt().toByte() // 将字符转换为Byte
        }.toByteArray()
    }
}
