package org.shoulder.core.dto.response;

import lombok.Data;

import java.io.Serializable;

/**
 * 树状数据
 * @author lym
 */
@Data
public class TreeNodeDTO implements Serializable {

	private String id;
	private String name;
	private String parentId;
	private String namePath;
	private Integer namePathLevel;
	private String path;
	private Integer disOrder;
	private String svgIcon;
	private Boolean auth;
	private Boolean leaf;
	private Boolean chkDisabled;
	private Boolean open;
	
}
