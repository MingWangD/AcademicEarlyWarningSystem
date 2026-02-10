package com.example.mapper;

import com.example.entity.AppUser;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {
    @Select("select id,username,password,email,name,role,risk_level from user where username=#{username}")
    AppUser findByUsername(String username);

    @Select("select id,username,password,email,name,role,risk_level from user where id=#{id}")
    AppUser findById(Long id);

    @Select("select id,username,password,email,name,role,risk_level from user order by id desc limit #{offset},#{limit}")
    List<AppUser> findPage(@Param("offset") int offset, @Param("limit") int limit);

    @Select("select count(1) from user")
    long countAll();

    @Insert("insert into user(username,password,email,name,role,risk_level) values(#{username},#{password},#{email},#{name},#{role},#{riskLevel})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AppUser user);

    @Update("update user set username=#{username},email=#{email},name=#{name},role=#{role},risk_level=#{riskLevel} where id=#{id}")
    int update(AppUser user);

    @Delete("delete from user where id=#{id}")
    int deleteById(Long id);

    @Update("update user set risk_level=#{riskLevel} where id=#{id}")
    int updateRiskLevel(@Param("id") Long id, @Param("riskLevel") String riskLevel);

    @Select("select id,username,password,email,name,role,risk_level from user where role='STUDENT'")
    List<AppUser> findAllStudents();
}
