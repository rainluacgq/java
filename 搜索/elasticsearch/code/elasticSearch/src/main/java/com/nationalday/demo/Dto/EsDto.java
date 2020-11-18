package com.nationalday.demo.Dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.stream.Stream;

/**
 * 功能描述：
 *
 * @Author: national day
 * @Date: 2020/11/8
 */

@Data
public class EsDto {
    Long Id;
    String title;
    String name;
}
