package cn.yuanbao.processalive;
// YbAidlInterface.aidl
// Declare any non-default types here with import statements

interface YbAidlInterface {
    // yb 用于双进程间通讯
    String getServiceName();
}
