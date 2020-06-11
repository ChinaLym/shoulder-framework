package org.shoulder.code.dto;

import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 验证码
 * todo refactor expireIn 改为 duration
 *
 * @author lym
 */
@ToString
public class ValidateCodeDTO implements Serializable {

	//private String type;

	private String code;
	
	private LocalDateTime expireTime;
	

	public ValidateCodeDTO(String code, long expireSeconds){
		this.code = code;
		this.expireTime = LocalDateTime.now().plusSeconds(expireSeconds);
	}

	public ValidateCodeDTO(String code, LocalDateTime expireTime){
		this.code = code;
		this.expireTime = expireTime;
	}

	/*public ValidateCodeDTO(String type, String code, long expireSeconds){
		this.type = type;
		this.code = code;
		this.expireTime = LocalDateTime.now().plusSeconds(expireSeconds);
	}

	public ValidateCodeDTO(String type, String code, LocalDateTime expireTime){
		this.type = type;
		this.code = code;
		this.expireTime = expireTime;
	}*/

	public boolean isExpire() {
		return LocalDateTime.now().isAfter(expireTime);
	}


	// ----------- getter | setter --------------

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public LocalDateTime getExpireTime() {
		return expireTime;
	}

	public void setExpireTime(LocalDateTime expireTime) {
		this.expireTime = expireTime;
	}

	/*@Override
	public String getType() {
		return type;
	}

	public void setType(ValidateCodeType type) {
		this.type = type.getType();
	}
	public void setType(String type) {
		this.type = type;
	}*/


}