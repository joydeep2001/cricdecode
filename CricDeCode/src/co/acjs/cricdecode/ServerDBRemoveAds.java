package co.acjs.cricdecode;

import com.stackmob.sdk.model.StackMobModel;

public class ServerDBRemoveAds extends StackMobModel {
	String	user_id;
	String	order_id;
	String	token;
	String	sign;
	int		purchaseTime;

	public ServerDBRemoveAds(String user_id, String order_id, String token, String sign, int purchaseTime) {
		super(ServerDBRemoveAds.class);
		this.user_id = user_id;
		this.order_id = order_id;
		this.token = token;
		this.sign = sign;
		this.purchaseTime = purchaseTime;
	}

	public void setId(String user_id) {
		this.user_id = user_id;
	}

	public void setOrderId(String order_id) {
		this.order_id = order_id;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public void setSign(String sign) {
		this.sign = sign;
	}

	public void setPurchaseTime(int purchaseTime) {
		this.purchaseTime = purchaseTime;
	}

	public String getId() {
		return this.user_id;
	}

	public String getOrderId() {
		return this.order_id;
	}

	public String getToken() {
		return this.token;
	}

	public String getSign() {
		return this.sign;
	}

	public int getPurchaseTime() {
		return this.purchaseTime;
	}

}