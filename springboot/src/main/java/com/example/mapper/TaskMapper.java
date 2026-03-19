package com.example.mapper;

import com.example.entity.Task;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TaskMapper {
    @Insert("insert into task(course_id,type,title,details,due_date,created_by) values(#{courseId},#{type},#{title},#{details},#{dueDate},#{createdBy})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Task task);

    @Select("select id,course_id,type,title,details,due_date,created_by from task order by id desc limit #{limit} offset #{offset}")
    List<Task> findPage(@Param("limit") Integer limit, @Param("offset") Integer offset);

    @Select("select count(1) from task")
    long countAll();

    @Select("select id,course_id,type,title,details,due_date,created_by from task where id=#{id}")
    Task findById(Long id);
    @Select("select id,course_id,type,title,details,due_date,created_by from task where title=#{title} limit 1")
    Task findByTitle(@Param("title") String title);
}
