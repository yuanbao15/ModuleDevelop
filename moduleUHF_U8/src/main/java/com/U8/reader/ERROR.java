package com.U8.reader;

public class ERROR {
	public final static byte SUCCESS = 0x10;
	public final static byte FAIL = 0x11;
	public final static byte MCU_RESET_ERROR = 0x20;
	public final static byte CW_ON_ERROR = 0x21;
	public final static byte ANTENNA_MISSING_ERROR = 0x22;
	public final static byte WRITE_FLASH_ERROR = 0x23;
	public final static byte READ_FLASH_ERROR = 0x24;
	public final static byte SET_OUTPUT_POWER_ERROR = 0x25;
	public final static byte TAG_INVENTORY_ERROR = 0x31;
	public final static byte TAG_READ_ERROR = 0x32;
	public final static byte TAG_WRITE_ERROR = 0x33;
	public final static byte TAG_LOCK_ERROR = 0x34;
	public final static byte TAG_KILL_ERROR = 0x35;
	public final static byte NO_TAG_ERROR = 0x36;
	public final static byte INVENTORY_OK_BUT_ACCESS_FAIL = 0x37;
	public final static byte BUFFER_IS_EMPTY_ERROR = 0x38;
	public final static byte ACCESS_OR_PASSWORD_ERROR = 0x40;
	public final static byte PARAMETER_INVALID = 0x41;
	public final static byte PARAMETER_INVALID_WORDCNT_TOO_LONG = 0x42;
	public final static byte PARAMETER_INVALID_MEMBANK_OUT_OF_RANGE = 0x43;
	public final static byte PARAMETER_INVALID_LOCK_REGION_OUT_OF_RANGE = 0x44;
	public final static byte PARAMETER_INVALID_LOCK_ACTION_OUT_OF_RANGE = 0x45;
	public final static byte PARAMETER_READER_ADDRESS_INVALID = 0x46;
	public final static byte PARAMETER_INVALID_ANTENNA_ID_OUT_OF_RANGE = 0x47;
	public final static byte PARAMETER_INVALID_OUTPUT_POWER_OUT_OF_RANGE = 0x48;
	public final static byte PARAMETER_INVALID_FREQUENCY_REGION_OUT_OF_RANGE = 0x49;
	public final static byte PARAMETER_INVALID_BAUDRATE_OUT_OF_RANGE = 0x4A;
	public final static byte PARAMETER_BEEPER_MODE_OUT_OF_RANGE = 0x4B;
	public final static byte PARAMETER_EPC_MATCH_LEN_TOO_LONG = 0x4C;
	public final static byte PARAMETER_EPC_MATCH_LEN_ERROR = 0x4D;
	public final static byte PARAMETER_INVALID_EPC_MATCH_MODE = 0x4E;
	public final static byte PARAMETER_INVALID_FREQUENCY_RANGE = 0x4F;
	public final static byte FAIL_TO_GET_RN16_FROM_TAG = 0x50;
	public final static byte PARAMETER_INVALID_DRM_MODE = 0x51;
	public final static byte PLL_LOCK_FAIL = 0x52;
	public final static byte RF_CHIP_FAIL_TO_RESPONSE = 0x53;
	public final static byte FAIL_TO_ACHIEVE_DESIRED_OUTPUT_POWER = 0x54;
	public final static byte COPYRIGHT_AUTHENTICATION_FAIL = 0x55;
	public final static byte SPECTRUM_REGULATION_ERROR = 0x56;
	public final static byte OUTPUT_POWER_TOO_LOW = 0x57;
	
	/**
	 * 自定义错误: 未接受完整错误
	 */
	public final static String RECEVICE_INCOMPLETE = "接收数据异常";
	/**
	 * 自定义错误: EPC绑定/解绑失败
	 */
	public final static String EPC_MATCH_ERROR = "EPC绑定/解绑失败";
	
	/**
	 * 根据错误代码获取错误类型
	 * @param btErrorCode 错误代码(1 byte)
	 * @return 错误类型说明
	 */
	public static String format(byte btErrorCode)
    {
		String strErrorCode = "";
        switch (btErrorCode)
        {
            case SUCCESS:
                strErrorCode = "命令成功完成";
                break;
            case FAIL:
                strErrorCode = "命令执行失败";//
                break;
            case MCU_RESET_ERROR:
                strErrorCode = "CPU 复位错误";//
                break;
            case CW_ON_ERROR:
                strErrorCode = "打开CW 错误";//
                break;
            case ANTENNA_MISSING_ERROR:
                strErrorCode = "天线未连接";//
                break;
            case WRITE_FLASH_ERROR:
                strErrorCode = "写Flash 错误";//
                break;
            case READ_FLASH_ERROR:
                strErrorCode = "读Flash 错误";//
                break;
            case SET_OUTPUT_POWER_ERROR:
                strErrorCode = "设置发射功率错误";//
                break;
            case TAG_INVENTORY_ERROR:
                strErrorCode = "盘存标签错误";//
                break;
            case TAG_READ_ERROR:
                strErrorCode = "读标签错误";//
                break;
            case TAG_WRITE_ERROR:
                strErrorCode = "写标签错误";//
                break;
            case TAG_LOCK_ERROR:
                strErrorCode = "锁定标签错误";//
                break;
            case TAG_KILL_ERROR:
                strErrorCode = "灭活标签错误";//
                break;
            case NO_TAG_ERROR:
                strErrorCode = "无可操作标签错误";//
                break;
            case INVENTORY_OK_BUT_ACCESS_FAIL:
                strErrorCode = "成功盘存但访问失败";//
                break;
            case BUFFER_IS_EMPTY_ERROR:
                strErrorCode = "缓存为空";//
                break;
            case ACCESS_OR_PASSWORD_ERROR:
                strErrorCode = "访问标签错误或访问密码错误";//
                break;
            case PARAMETER_INVALID:
                strErrorCode = "无效的参数";//
                break;
            case PARAMETER_INVALID_WORDCNT_TOO_LONG:
                strErrorCode = "wordCnt 参数超过规定长度";//
                break;
            case PARAMETER_INVALID_MEMBANK_OUT_OF_RANGE:
                strErrorCode = "MemBank 参数超出范围";//
                break;
            case PARAMETER_INVALID_LOCK_REGION_OUT_OF_RANGE:
                strErrorCode = "Lock 数据区参数超出范围";//
                break;
            case PARAMETER_INVALID_LOCK_ACTION_OUT_OF_RANGE:
                strErrorCode = "LockType 参数超出范围";//
                break;
            case PARAMETER_READER_ADDRESS_INVALID:
                strErrorCode = "读写器地址无效";//
                break;
            case PARAMETER_INVALID_ANTENNA_ID_OUT_OF_RANGE:
                strErrorCode = "Antenna_id 超出范围";//
                break;
            case PARAMETER_INVALID_OUTPUT_POWER_OUT_OF_RANGE:
                strErrorCode = "输出功率参数超出范围";//
                break;
            case PARAMETER_INVALID_FREQUENCY_REGION_OUT_OF_RANGE:
                strErrorCode = "射频规范区域参数超出范围";//
                break;
            case PARAMETER_INVALID_BAUDRATE_OUT_OF_RANGE:
                strErrorCode = "波特率参数超出范围";//
                break;
            case PARAMETER_BEEPER_MODE_OUT_OF_RANGE:
                strErrorCode = "蜂鸣器设置参数超出范围";//
                break;
            case PARAMETER_EPC_MATCH_LEN_TOO_LONG:
                strErrorCode = "EPC 匹配长度越界";//
                break;
            case PARAMETER_EPC_MATCH_LEN_ERROR:
                strErrorCode = "EPC 匹配长度错误";//
                break;
            case PARAMETER_INVALID_EPC_MATCH_MODE:
                strErrorCode = "EPC 匹配参数超出范围";//
                break;
            case PARAMETER_INVALID_FREQUENCY_RANGE:
                strErrorCode = "频率范围设置参数错误";//
                break;
            case FAIL_TO_GET_RN16_FROM_TAG:
                strErrorCode = "无法接收标签的RN16";//
                break;
            case PARAMETER_INVALID_DRM_MODE:
                strErrorCode = "DRM 设置参数错误";//
                break;
            case PLL_LOCK_FAIL:
                strErrorCode = "PLL 不能锁定";//
                break;
            case RF_CHIP_FAIL_TO_RESPONSE:
                strErrorCode = "射频芯片无响应";//
                break;
            case FAIL_TO_ACHIEVE_DESIRED_OUTPUT_POWER:
                strErrorCode = "输出达不到指定的输出功率";//
                break;
            case COPYRIGHT_AUTHENTICATION_FAIL:
                strErrorCode = "版权认证未通过";//
                break;
            case SPECTRUM_REGULATION_ERROR:
                strErrorCode = "频谱规范设置错误";//
                break;
            case OUTPUT_POWER_TOO_LOW:
                strErrorCode = "输出功率过低";//
                break;
            default:
            	strErrorCode = "未知错误";//
                break;
        }
        return strErrorCode;
    }
}
