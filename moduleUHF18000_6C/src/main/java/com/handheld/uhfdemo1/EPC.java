package com.handheld.uhfdemo1;

public class EPC {
	private int id;
	private String epc;
	private int count;
	private int rssi;

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}
	/**
	 * @return the epc
	 */
	public String getEpc() {
		return epc;
	}
	/**
	 * @param epc the epc to set
	 */
	public void setEpc(String epc) {
		this.epc = epc;
	}
	/**
	 * @return the count
	 */
	public int getCount() {
		return count;
	}
	/**
	 * @param count the count to set
	 */
	public void setCount(int count) {
		this.count = count;
	}

	/**
	 *
	 * @return tag's rssi
	 */
	public int getRssi() {
		return rssi;
	}

	/**
	 * rssi values
	 * @param rssi
	 */
	public void setRssi(int rssi) {
		this.rssi = rssi;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "EPC [id=" + id + ", epc=" + epc + ", count=" + count + "]";
	}

}
