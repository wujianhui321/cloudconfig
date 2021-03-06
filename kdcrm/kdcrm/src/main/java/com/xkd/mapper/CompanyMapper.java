package com.xkd.mapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.xkd.model.Company;
import org.springframework.stereotype.Repository;

public interface CompanyMapper {

	List<Company> selectCompanyByName(@Param("companyName") String companyName,@Param("pcCompanyId") String pcCompanyId);
	List<String> selectCompanyNamesList();

 
	int updateCompanyInfoById(Map<String, Object> company);
	
	int updateCompanyDetailInfoById(Map<String, Object> companyDetail);

 
	int deleteCompanyById(@Param("ids") String ids);

 
	Company selectCompanyInfoById(@Param("companyId") String companyId);

	List<Company> selectCompanyInfoByIdList(@Param("idList") List<String> idList);

	Integer getTotalRows(@Param("content") String content, @Param("industryId") String industryId,
						 @Param("investStatus") String investStatus, @Param("companyIds") String area, @Param("establishTime") String establishTime,
						 @Param("saveTime") String saveTime, @Param("currentPage") String currentPage, @Param("pageSize") String pageSize,
						 @Param("pcompanyIds") String pcompanyIds, @Param("ucompanyIds") String ucompanyIds);

	Integer updateCompanyBySql(@Param("sql") String sql);

  List<Map<String,Object>>  searchCompanyByName(@Param("companyName")String companyName,@Param("departmentIdList")List<String> departmentIdList,@Param("start")Integer start,@Param("pageSize")Integer pageSize);

	List<HashMap<String, Object>> selectCompanyByNameMH(@Param("companyName") String companyName,@Param("pcCompanyId") String pcCompanyId);
	
	List<Company> selectCompanyByNameIncludingDeleted(@Param("companyName") String companyName,@Param("pcCompanyId")String pcCompanyId);

	Integer insertCompanyInfo(Map<String, Object> company);
	
	Integer insertCompanyDetailInfo(Map<String, Object> companyDetail);
	
	void deleteByCompanyById(@Param("id") String id);

	List<String> selecAllCompanyId();

 
    List<String> selectSolrCompanyIdsByDepartmentIdsAndOperate (@Param("departmentIdList") List<String> departmentIdList,@Param("operate") Integer operate,@Param("start")Integer start,@Param("pageSize")Integer pageSize);
	int deleteSolrCompanyIds (@Param("idList")List<String> idList,@Param("operate") Integer operate);



	public int insertDcSolrCompany(@Param("departmentIdList")List<String> departmentIdList,@Param("operate") Integer operate);






	List<String> selectSonIndustrys();
	
	Integer updateCompanyLabelById(@Param("companyId") String companyId, @Param("label") String label);
	
	
	//查询爬的一些只读详细信息
	Map<String,String>   selectCrawlInfo(@Param("id") String id);
	//修改企业文件夹名称
	void updatePagerFileName(Map<String, Object> company);
	
	List<Company> selectAllStatusCompanyByName(@Param("companyName") String companyName,@Param("pcCompanyId") String pcCompanyId);

    List<HashMap<String,Object>> checkCompany(@Param("companyName") String companyName,@Param("pcCompanyId") String pcCompanyId);


	List<String> selectAllCompanyIds();
}
