package com.nowbook.restful.dto;

import lombok.*;

import java.io.Serializable;

/**
 * Mail: cheng@nowbook.com <br>
 * Date: 2014-06-10 9:52 PM  <br>
 * Author:cheng
 */
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class NbUserDto implements Serializable {


    private static final long serialVersionUID = -7649075753473440068L;

    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private String mobile;

    @Getter
    @Setter
    private String email;

    @Getter
    @Setter
    private String token;

}
