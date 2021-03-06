package com.xkd.model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class Company implements Serializable {

	private static final long serialVersionUID = 1L;
	private String id;
	private String  companyName;
	private String  socialCredit;
	private String  registrationCode;
	private String  organizationCode ;
	private String  manageType ;
	private String  companyProperty ;
	private String  companyPropertyName;
	private String  manageScope ;
	private String  parentIndustryId;
	private String  parentIndustry;
	private String  sonIndustry;
	private String  representative;
	private String  registeredMoney ;
	private String  establishTime ;
	private String  termStart ;
	private String operatingPeriod;
	private String registrationAuthority;
	private String  annualSalesVolume ;
	private String  annualProfit;
	private String thisYearSalesVolume;
	private String nextYearSalesVolume;
	private String businessScope;
	private String approveDate;
	private String companyType ;
	private String companySize ;
	private String investStatus;
	private String financeStatus;
	private String englishName ;
	private String beforeName ;
	private String companyDesc ;
	private String website;
	private String phone;
	private String email;
	private String wechat;
	private String content;
	private String province;
	private String dbChangeTime;
	private String econKind;
	private String label;
	private String companyAdviser;
	private String companyAdviserName;
	private String companyDirector;
	private String companyDirectorName;
	private String companyOpportunity;
	private String following;
	private String companyPosition;
	private Integer status;
	private String logo;
	private Integer learnStatus;
	private String country;
	private String city;
	private String county;
	private String address ; 
	private String contactUserId;
	private String contactName;
	private String contactPhone;
	private String moneySituation;
	private String userLevel;
	private String userType;
	private String channel;
	private String paymentMoney;
	private String enrollDate;
	private String qccUpdatedDate;
	private String companyAdviserId;   
  	private String companyDirectorId;
  	private String createdBy;
	private String hasResource;
	private String needResource;
	private Integer infoScore;
	private String departmentId;
	private String departmentName;
	
	private List<Map<String,Object>>  relativeUserList;

	private  String createDate;
	private String createdByName;
	private  String priority;
	private  String attendStatus;
	

	
	//方便前台，将用户联系方式等信息，装入到object中
	private Object object;
	
	public Object getObject() {
		return object;
	}

	public void setObject(Object object) {
		this.object = object;
	}

	public String getId() {
		return id;
	}


	public void setId(String id) {
		this.id = id;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getSocialCredit() {
		return socialCredit;
	}

	public void setSocialCredit(String socialCredit) {
		this.socialCredit = socialCredit;
	}

	public String getRegistrationCode() {
		return registrationCode;
	}

	public void setRegistrationCode(String registrationCode) {
		this.registrationCode = registrationCode;
	}

	public String getOrganizationCode() {
		return organizationCode;
	}

	public void setOrganizationCode(String organizationCode) {
		this.organizationCode = organizationCode;
	}

	public String getManageType() {
		return manageType;
	}

	public void setManageType(String manageType) {
		this.manageType = manageType;
	}

	public String getCompanyProperty() {
		return companyProperty;
	}

	public void setCompanyProperty(String companyProperty) {
		this.companyProperty = companyProperty;
	}

	public String getManageScope() {
		return manageScope;
	}

	public void setManageScope(String manageScope) {
		this.manageScope = manageScope;
	}

	public String getParentIndustryId() {
		return parentIndustryId;
	}

	public void setParentIndustryId(String parentIndustryId) {
		this.parentIndustryId = parentIndustryId;
	}

 

	public String getParentIndustry() {
		return parentIndustry;
	}

	public void setParentIndustry(String parentIndustry) {
		this.parentIndustry = parentIndustry;
	}

	public String getSonIndustry() {
		return sonIndustry;
	}

	public void setSonIndustry(String sonIndustry) {
		this.sonIndustry = sonIndustry;
	}

	public String getRepresentative() {
		return representative;
	}

	public void setRepresentative(String representative) {
		this.representative = representative;
	}

	public String getRegisteredMoney() {
		return registeredMoney;
	}

	public void setRegisteredMoney(String registeredMoney) {
		this.registeredMoney = registeredMoney;
	}

	public String getEstablishTime() {
		return establishTime;
	}

	public void setEstablishTime(String establishTime) {
		this.establishTime = establishTime;
	}

	public String getTermStart() {
		return termStart;
	}

	public void setTermStart(String termStart) {
		this.termStart = termStart;
	}

	public String getOperatingPeriod() {
		return operatingPeriod;
	}

	public void setOperatingPeriod(String operatingPeriod) {
		this.operatingPeriod = operatingPeriod;
	}

	public String getRegistrationAuthority() {
		return registrationAuthority;
	}

	public void setRegistrationAuthority(String registrationAuthority) {
		this.registrationAuthority = registrationAuthority;
	}

	public String getAnnualSalesVolume() {
		return annualSalesVolume;
	}

	public void setAnnualSalesVolume(String annualSalesVolume) {
		this.annualSalesVolume = annualSalesVolume;
	}

	public String getAnnualProfit() {
		return annualProfit;
	}

	public void setAnnualProfit(String annualProfit) {
		this.annualProfit = annualProfit;
	}

	public String getApproveDate() {
		return approveDate;
	}

	public void setApproveDate(String approveDate) {
		this.approveDate = approveDate;
	}

	public String getCompanyType() {
		return companyType;
	}

	public void setCompanyType(String companyType) {
		this.companyType = companyType;
	}

	public String getCompanySize() {
		return companySize;
	}

	public void setCompanySize(String companySize) {
		this.companySize = companySize;
	}

	public String getInvestStatus() {
		return investStatus;
	}

	public void setInvestStatus(String investStatus) {
		this.investStatus = investStatus;
	}

	public String getFinanceStatus() {
		return financeStatus;
	}

	public void setFinanceStatus(String financeStatus) {
		this.financeStatus = financeStatus;
	}

	public String getEnglishName() {
		return englishName;
	}

	public void setEnglishName(String englishName) {
		this.englishName = englishName;
	}

	public String getBeforeName() {
		return beforeName;
	}

	public void setBeforeName(String beforeName) {
		this.beforeName = beforeName;
	}

	public String getCompanyDesc() {
		return companyDesc;
	}

	public void setCompanyDesc(String companyDesc) {
		this.companyDesc = companyDesc;
	}

	public String getWebsite() {
		return website;
	}

	public void setWebsite(String website) {
		this.website = website;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getWechat() {
		return wechat;
	}

	public void setWechat(String wechat) {
		this.wechat = wechat;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public String getDbChangeTime() {
		return dbChangeTime;
	}

	public void setDbChangeTime(String dbChangeTime) {
		this.dbChangeTime = dbChangeTime;
	}

	 

	public String getEconKind() {
		return econKind;
	}

	public void setEconKind(String econKind) {
		this.econKind = econKind;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getCompanyAdviser() {
		return companyAdviser;
	}

	public void setCompanyAdviser(String companyAdviser) {
		this.companyAdviser = companyAdviser;
	}

	public String getCompanyAdviserName() {
		return companyAdviserName;
	}

	public void setCompanyAdviserName(String companyAdviserName) {
		this.companyAdviserName = companyAdviserName;
	}

	public String getCompanyDirector() {
		return companyDirector;
	}

	public void setCompanyDirector(String companyDirector) {
		this.companyDirector = companyDirector;
	}

	public String getCompanyDirectorName() {
		return companyDirectorName;
	}

	public void setCompanyDirectorName(String companyDirectorName) {
		this.companyDirectorName = companyDirectorName;
	}

	public String getCompanyOpportunity() {
		return companyOpportunity;
	}

	public void setCompanyOpportunity(String companyOpportunity) {
		this.companyOpportunity = companyOpportunity;
	}

	public String getFollowing() {
		return following;
	}

	public void setFollowing(String following) {
		this.following = following;
	}

	public String getCompanyPosition() {
		return companyPosition;
	}

	public void setCompanyPosition(String companyPosition) {
		this.companyPosition = companyPosition;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getLogo() {
		return logo;
	}

	public void setLogo(String logo) {
		this.logo = logo;
	}

	public Integer getLearnStatus() {
		return learnStatus;
	}

	public void setLearnStatus(Integer learnStatus) {
		this.learnStatus = learnStatus;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getCounty() {
		return county;
	}

	public void setCounty(String county) {
		this.county = county;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getContactUserId() {
		return contactUserId;
	}

	public void setContactUserId(String contactUserId) {
		this.contactUserId = contactUserId;
	}

	public String getContactName() {
		return contactName;
	}

	public void setContactName(String contactName) {
		this.contactName = contactName;
	}

	public String getContactPhone() {
		return contactPhone;
	}

	public void setContactPhone(String contactPhone) {
		this.contactPhone = contactPhone;
	}

	public String getCompanyPropertyName() {
		return companyPropertyName;
	}

	public void setCompanyPropertyName(String companyPropertyName) {
		this.companyPropertyName = companyPropertyName;
	}

	public String getThisYearSalesVolume() {
		return thisYearSalesVolume;
	}

	public void setThisYearSalesVolume(String thisYearSalesVolume) {
		this.thisYearSalesVolume = thisYearSalesVolume;
	}

	public String getNextYearSalesVolume() {
		return nextYearSalesVolume;
	}

	public void setNextYearSalesVolume(String nextYearSalesVolume) {
		this.nextYearSalesVolume = nextYearSalesVolume;
	}

	public String getBusinessScope() {
		return businessScope;
	}

	public void setBusinessScope(String businessScope) {
		this.businessScope = businessScope;
	}

	public String getMoneySituation() {
		return moneySituation;
	}

	public void setMoneySituation(String moneySituation) {
		this.moneySituation = moneySituation;
	}

	public String getUserLevel() {
		return userLevel;
	}

	public void setUserLevel(String userLevel) {
		this.userLevel = userLevel;
	}

	public String getUserType() {
		return userType;
	}

	public void setUserType(String userType) {
		this.userType = userType;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public String getPaymentMoney() {
		return paymentMoney;
	}

	public void setPaymentMoney(String paymentMoney) {
		this.paymentMoney = paymentMoney;
	}

	public String getEnrollDate() {
		return enrollDate;
	}

	public void setEnrollDate(String enrollDate) {
		this.enrollDate = enrollDate;
	}

	public String getQccUpdatedDate() {
		return qccUpdatedDate;
	}

	public void setQccUpdatedDate(String qccUpdatedDate) {
		this.qccUpdatedDate = qccUpdatedDate;
	}

	public String getCompanyAdviserId() {
		return companyAdviserId;
	}

	public void setCompanyAdviserId(String companyAdviserId) {
		this.companyAdviserId = companyAdviserId;
	}

	public String getCompanyDirectorId() {
		return companyDirectorId;
	}

	public void setCompanyDirectorId(String companyDirectorId) {
		this.companyDirectorId = companyDirectorId;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public List<Map<String, Object>> getRelativeUserList() {
		return relativeUserList;
	}

	public void setRelativeUserList(List<Map<String, Object>> relativeUserList) {
		this.relativeUserList = relativeUserList;
	}

	public String getHasResource() {
		return hasResource;
	}

	public void setHasResource(String hasResource) {
		this.hasResource = hasResource;
	}

	public String getNeedResource() {
		return needResource;
	}

	public void setNeedResource(String needResource) {
		this.needResource = needResource;
	}

	public Integer getInfoScore() {
		return infoScore;
	}

	public void setInfoScore(Integer infoScore) {
		this.infoScore = infoScore;
	}

	public String getDepartmentId() {
		return departmentId;
	}

	public void setDepartmentId(String departmentId) {
		this.departmentId = departmentId;
	}

	public String getDepartmentName() {
		return departmentName;
	}

	public void setDepartmentName(String departmentName) {
		this.departmentName = departmentName;
	}

	public String getCreateDate() {
		return createDate;
	}

	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}

	public String getCreatedByName() {
		return createdByName;
	}

	public void setCreatedByName(String createdByName) {
		this.createdByName = createdByName;
	}

	public String getPriority() {
		return priority;
	}

	public void setPriority(String priority) {
		this.priority = priority;
	}

	public String getAttendStatus() {
		return attendStatus;
	}

	public void setAttendStatus(String attendStatus) {
		this.attendStatus = attendStatus;
	}
}
