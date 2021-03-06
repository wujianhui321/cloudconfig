package com.xkd.mapper;

import com.xkd.model.SpreadSetting;
import com.xkd.model.SpreadUser;
import com.xkd.model.UserGetMoney;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface SpreadMapper {

    Integer insertSpreadSetting(SpreadSetting spreadSetting);

    Integer updateSpreadSetting(SpreadSetting spreadSetting);

    List<SpreadSetting> selectSpreadSettings(Map<String, Object> paramMap);

    Integer selectSpreadSettingsTotal(Map<String, Object> paramMap);

    Integer insertSpreadUser(SpreadUser spreadUser);

    SpreadUser selectSpreadUserByMobile(@Param("mobile") String mobile);

    Integer updateSpreadUser(SpreadUser spreadUser);

    SpreadUser selectSpreadUserById(@Param("id")String id);

    SpreadSetting selectSpreadSettingById(@Param("id")String id);

    List<UserGetMoney> selectSpreadUserGetMoneyLogs(Map<String, Object> paramMap);

    Integer insertGetMoneyLog(UserGetMoney userGetMoney);

    Integer selectSpreadUserGetMoneyLogsTotal(Map<String, Object> paramMap);

    Map<String,Object> selectSpreadGetMoneyLogDetail(@Param("userGetMoneyLogId")String userGetMoneyLogId);

    List<Map<String,Object>> selectSpreadUsers(Map<String, Object> map);

    Integer selectSpreadUsersTotal(Map<String, Object> map);

    List<Map<String,Object>> selectPcSpreadUserDetail(Map<String, Object> map);

    Integer selectPcSpreadUserDetailTotal(Map<String, Object> map);

    List<Map<String,Object>> selectSpreadUserlogs(Map<String, Object> map);

    Integer selectSpreadUserlogsTotal(Map<String, Object> map);

    List<Map<String,Object>> selectGetMoneys(Map<String, Object> map);

    Integer selectGetMoneysTotal(Map<String, Object> paramMap);

    UserGetMoney selectGetMoneyLogById(@Param("id")String id);

    Integer updateGetMoneyLog(UserGetMoney userGetMoney);

    SpreadUser selectSpreadUserByOpenId(@Param("openId")String openId);
}
