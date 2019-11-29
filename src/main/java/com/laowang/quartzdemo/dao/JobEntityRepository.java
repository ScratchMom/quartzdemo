package com.laowang.quartzdemo.dao;

/**
 * @author laowang
 * @date 2019/11/28 5:23 PM
 * @Description:
 */

import com.laowang.quartzdemo.entity.JobEntity;
import org.springframework.data.repository.CrudRepository;

/**
 * Created by EalenXie on 2018/6/4 14:27
 */
public interface JobEntityRepository extends CrudRepository<JobEntity, Long> {
    JobEntity getById(Integer id);
}
