package com.U8.operation;

import com.U8.model.IResponseHandler;
import com.U8.model.Message;

public interface IUSeries {
	
	/**
	 * 打开串口
	 * @param moduleName 模块名
	 * @return true_ 打开串口成功,false_ 打开串口失败
	 */
	Message openSerialPort(String moduleName);

	/**
	 * 关闭串口
	 * 
	 * @return true_ 关闭串口成功,false_ 关闭串口失败
	 */
	Message closeSerialPort();

	/**
	 * 模块上电
	 * @param moduleName 模块名
	 * @return true_ 上电成功,false_ 上电失败
	 */
	Message modulePowerOn(String moduleName);

	/**
	 * 模块下电
	 * @param moduleName 模块名
	 * @return true_ 下电成功,false_ 下电失败
	 */
	Message modulePowerOff(String moduleName);

	/**
	 * 开始盘询
	 * @param responseHandler 盘询结果回调
	 * @return true_ 开始盘询成功成功,false_ 开始盘询失败
	 */
	boolean startInventory(IResponseHandler responseHandler);

	/**
	 * 停止盘询
	 * @return true_ 停止盘询成功,false_ 停止盘询失败
	 */
	boolean stopInventory();

	/**
	 * 单次盘询
	 * @return 盘询结果
	 */
	Message Inventory();

	/**
	 * 读标签
	 * 
	 * @param block
	 *            读取区域
	 * @param w_count
	 *            读取长度
	 * @param w_offset
	 *            偏移
	 * @param acs_pwd
	 *            访问密码
	 * @return 读取标签数据
	 */
	Message readTagMemory(byte[] EPC, byte block, byte w_count, byte w_offset, byte[] acs_pwd);

	/**
	 * 写标签
	 * 
	 * @param block
	 *            写入区域
	 * @param w_count
	 *            写入长度
	 * @param w_offset
	 *            偏移
	 * @param data
	 *            写入数据
	 * @param acs_pwd
	 *            访问密码
	 * @return 是否写入成功
	 */
	Message writeTagMemory(byte[] EPC, byte block, byte w_count, byte w_offset, byte[] data, byte[] acs_pwd);

	/**
	 * 锁标签
	 * 
	 * @param block
	 *            锁定区域
	 * @param opration
	 *            操作类型
	 * @param acs_pwd
	 *            访问密码
	 * @return 返回错误代码
	 */
	Message lockTagMemory(byte[] EPC, byte block, Enum operation, byte[] acs_pwd);

	/**
	 * 销毁标签
	 * 
	 * @param kill_pwd
	 *            销毁密码
	 * @return 返回错误代码
	 */
	Message killTag(byte[] EPC, byte[] kill_pwd);

	/**
	 * 设置参数
	 * 
	 * @param paraName
	 *            参数名(详见SDK)
	 * @return
	 */
	String getParams(String paraName);

	/**
	 * 设置参数
	 * 
	 * @param paraName
	 *            参数名(详见SDK)
	 * @param paraValue
	 *            参数值(详见SDK)
	 * @return
	 */
	boolean setParams(String paraName, String paraValue);


}
