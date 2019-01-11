package com.webtech.service.alert.dto;

import org.springframework.data.cassandra.core.mapping.Table;

@Table("alert_comment")
public class LiveAlertCommentDTO extends BaseComment {

}
