

# txf-allclub

## 介绍

txf-allclub 是一个综合型俱乐部管理平台，包含用户权限管理、社交分享、练习题库、微信服务等多个模块。该项目基于Spring Boot、MyBatis Plus、Redis、Feign等主流Java技术栈构建，支持微服务架构与分布式部署。

### 主要功能模块

1. **用户权限模块 (allclub-auth)**  
   - 用户注册、登录、信息管理
   - 角色管理、权限分配
   - 使用Redis进行用户状态与权限缓存
   - 支持拦截器控制接口权限访问

2. **社交分享模块 (allclub-circle)**  
   - 社交圈子创建、管理
   - 发布、评论、消息通知
   - 敏感词过滤机制
   - WebSocket 实时通信支持

3. **练习题库模块 (allclub-practice)**  
   - 题目分类、标签管理
   - 练习记录、答题提交
   - 支持多种题型（单选、多选、判断等）
   - 排行榜与答题报告功能

4. **微信服务 (allclub-wx)**  
   - 微信消息回调处理
   - 支持订阅与文本消息处理
   - 用户消息解析与路由

5. **作业调度 (xxl-job)**  
   - 支持定时任务调度
   - 提供作业管理、日志、失败监控等
   - 支持多种调度策略（轮询、一致性哈希等）

## 技术架构

- **Spring Boot** 作为基础框架
- **MyBatis Plus** 数据库操作层
- **Redis** 用户信息、权限、敏感词等缓存
- **Feign** 模块间通信
- **WebSocket** 实时消息推送
- **RocketMQ** 异步消息队列
- **XXL-JOB** 定时任务调度平台

## 安装与部署

1. 克隆项目：
   ```bash
   git clone https://gitee.com/quercus-sp204/txf-allclub.git
   ```

2. 构建Maven项目：
   ```bash
   cd txf-allclub
   mvn clean install
   ```

3. 配置数据库、Redis、MQ等服务（查看 `application.yml` 或 `bootstrap.yml`）

4. 启动各个模块：
   ```bash
   # 启动认证服务
   cd allclub-auth/auth-starter
   mvn spring-boot:run

   # 启动社交模块
   cd allclub-circle/circle-server
   mvn spring-boot:run

   # 启动微信服务
   cd allclub-wx
   mvn spring-boot:run

   # 启动定时任务调度
   cd xxl-job/xxl-job-admin
   mvn spring-boot:run
   ```

## 贡献指南

欢迎提交 Pull Request 或 Issues，贡献代码请遵循如下规则：
- 提交前请确保代码格式统一
- 注释清晰，接口文档完善
- 提交PR请说明修改内容与目的

## Gitee 特性支持

- **代码审查（Code Review）**
- **CI/CD 集成**
- **Issue �

 0")
  - public void setCode(int code)
  - public String getMessage()
  - public void setMessage(String message)
  - public T getData()
  - public void setData(T data)
  - public static Result ok()
  - public static <T> Result ok(T data)
  - public static Result fail()
  - public static <T> Result fail(T data)

### allclub-interview/interview-server/src/main/java/com/feng/interview/server/entity/po/SubjectMappingPO.java
@Data
public class SubjectMappingPO implements Serializable
  - private static final long serialVersionUID
  - private Long id
  - private Long subjectId
  - private Long categoryId
  - private Long labelId
  - private String createdBy
  - private Date createdTime
  - private String updateBy
  - private Integer isDeleted
  - private Date updateTime

### allclub-interview/interview-server/src/main/java/com/feng/interview/server/rpc/UserRpc.java
@Component
public class UserRpc
  - @Resource
    private UserFeignService userFeignService
  - public UserInfo getUserInfo(String userName)
  - public Map<String, UserInfo> batchGetUserInfo(List<String> userNameList)

### allclub-interview/interview-server/src/main/java/com/feng/interview/server/service/InterviewEngine.java
public interface InterviewEngine

### allclub-interview/interview-server/src/main/java/com/feng/interview/server/service/InterviewHistoryService.java
public interface InterviewHistoryService extends IService<InterviewHistory>

### allclub-interview/interview-server/src/main/java/com/feng/interview/server/service/InterviewQuestionHistoryService.java
public interface InterviewQuestionHistoryService extends IService<InterviewQuestionHistory>

### allclub-interview/interview-server/src/main/java/com/feng/interview/server/service/InterviewService.java
public interface InterviewService

### allclub-interview/interview-server/src/main/java/com/feng/interview/server/service/impl/AlBLInterviewEngine.java
@Service
@Slf4j
@SuppressWarnings("all")
public class AlBLInterviewEngine implements InterviewEngine
  - private static final String apiKey
  - @Override
    public EngineEnum engineType()
  - @Override
    public InterviewVO analyse(List<String> KeyWords)
  - @Override
    public InterviewResultVO submit(InterviewSubmitReq req)
  - @Override
    public InterviewQuestionVO start(StartReq req)
  - private static InterviewSubmitReq.Submit buildInterviewScore(InterviewSubmitReq.Submit submit)
  - private static InterviewQuestionVO.Interview buildInterview(String keyword)

### allclub-interview/interview-server/src/main/java/com/feng/interview/server/service/impl/InterviewHistoryServiceImpl.java
@Service("interviewHistoryService")
public class InterviewHistoryServiceImpl extends ServiceImpl<InterviewHistoryDao, InterviewHistory> implements InterviewHistoryService
  - @Resource
    private InterviewHistoryDao interviewHistoryDao
  - @Resource
    private InterviewQuestionHistoryDao interviewQuestionHistoryDao
  - @Override
    @Transactional(rollbackFor = Exception.class)
    public void logInterview(InterviewSubmitReq req, InterviewResultVO submit)
  - @Override
    public PageResult<InterviewHistoryVO> getHistory(InterviewHistoryReq req)

### allclub-interview/interview-server/src/main/java/com/feng/interview/server/service/impl/InterviewQuestionHistoryServiceImpl.java
@Service("interviewQuestionHistoryService")
public class InterviewQuestionHistoryServiceImpl extends ServiceImpl<InterviewQuestionHistoryDao, InterviewQuestionHistory> implements InterviewQuestionHistoryService
  - @Override
    public List<InterviewQuestionHistoryVO> detail(Long id)

### allclub-interview/interview-server/src/main/java/com/feng/interview/server/service/impl/InterviewServiceImpl.java
@Service
public class InterviewServiceImpl implements InterviewService, ApplicationContextAware
  - private static final Map<String, InterviewEngine> engineMap
  - @Resource
    private SubjectDao subjectDao
  - @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
  - @Override
    public InterviewVO analyse(InterviewReq req)
  - @Override
    public InterviewQuestionVO start(StartReq req)
  - @Override
    public InterviewResultVO submit(InterviewSubmitReq req)
  - private List<String> buildKeyWords(String url)

### allclub-interview/interview-server/src/main/java/com/feng/interview/server/service/impl/TXFInterviewEngine.java
@Service
public class TXFInterviewEngine implements InterviewEngine
  - private List<SubjectLabel> labels
  - private Map<Long, SubjectCategory> categoryMap
  - @PostConstruct
    public void init()
  - @Resource
    private SubjectDao subjectDao
  - @Override
    public EngineEnum engineType()
  - @Override
    public InterviewVO analyse(List<String> KeyWords)
  - @Override
    public InterviewQuestionVO start(StartReq req)
  - @Override
    public InterviewResultVO submit(InterviewSubmitReq req)

### allclub-interview/interview-server/src/main/java/com/feng/interview/server/util/DateUtils.java
@Slf4j
public class DateUtils
  - public static Date parseStrToDate(String timestamp)
  - public static String format(Date date, String format)

### allclub-interview/interview-server/src/main/java/com/feng/interview/server/util/DruidEncryptUtil.java
public class DruidEncryptUtil
  - private static String publicKey
  - private static String privateKey
  - public static String encrypt(String plainText) throws Exception
  - public static String decrypt(String encryptText) throws Exception
  - public static void main(String[] args) throws Exception

### allclub-interview/interview-server/src/main/java/com/feng/interview/server/util/EvaluateUtils.java
public class EvaluateUtils
  - private static final List<Evaluate> evaluates
  - private static List<Evaluate> build()
  - @Data
    @AllArgsConstructor
    private static class Evaluate
    - private double min
    - private double max
    - private String desc
  - public static String avgEvaluate(double score)
  - public static String evaluate(double score)

### allclub-interview/interview-server/src/main/java/com/feng/interview/server/util/HttpUtils.java
@Slf4j
public class HttpUtils
  - private HttpUtils()
  - private static CloseableHttpClient httpClient
  - private static CookieStore cookieStore
  - private static BasicCredentialsProvider basicCredentialsProvider
  - public static CloseableHttpClient getHttpclient()
  - public static CookieStore getCookieStore()
  - public static BasicCredentialsProvider getBasicCredentialsProvider()
  - public static String httpGet(String uri)
  - public static String httpGet(String uri, Map<String, String> cookieMap, Map<String, String> headMap)
  - @SneakyThrows
    public static void httpPostJson(String uri, Map<String, Object> reqArgs, Consumer<InputStream> reponseInputStream)
  - public static String executePost(String url, String reqArgs,
                                     Map<String, Object> headerMap)
  - private static String getStreamAsString(InputStream stream, String charset) throws IOException

### allclub-interview/interview-server/src/main/java/com/feng/interview/server/util/LoginUtil.java
public class LoginUtil
  - public static String getLoginId()

### allclub-interview/interview-server/src/main/java/com/feng/interview/server/util/PDFUtil.java
@Slf4j
public class PDFUtil
  - private static Pattern pattern
  - public static String getPdfText(String pdfUrl)

### allclub-interview/interview-server/src/main/java/com/feng/interview/server/util/keyword/EndType.java
public enum EndType

### allclub-interview/interview-server/src/main/java/com/feng/interview/server/util/keyword/FlagIndex.java
public class FlagIndex
  - private boolean flag
  - private boolean isWhiteWord
  - private List<Integer> index
  - public boolean isFlag()
  - public void setFlag(boolean flag)
  - public List<Integer> getIndex()
  - public void setIndex(List<Integer> index)
  - public boolean isWhiteWord()
  - public void setWhiteWord(boolean whiteWord)

### allclub-interview/interview-server/src/main/java/com/feng/interview/server/util/keyword/KeyWordUtil.java
public class KeyWordUtil
  - private final static Map wordMap
  - private static boolean init
  - public static boolean isInit()
  - public static List<String> buildKeyWordsLists(final String text)
  - private static FlagIndex getFlagIndex(final char[] charset, final int begin, final int skip)
  - public static void addWord(Collection<String> wordList)

### allclub-interview/interview-server/src/main/java/com/feng/interview/server/util/keyword/WordType.java
public enum WordType

### allclub-interview/interview-server/src/main/resources/application.yml
[NO MAP]

### allclub-interview/interview-server/src/main/resources/bootstrap.yml
[NO MAP]

### allclub-interview/interview-server/src/main/resources/log4j2-spring.xml
[NO MAP]

### allclub-interview/interview-server/src/main/resources/mapper/InterviewHistoryDao.xml
[NO MAP]

### allclub-interview/interview-server/src/main/resources/mapper/InterviewQuestionHistoryDao.xml
[NO MAP]

### allclub-interview/interview-server/src/main/resources/mapper/SubjectDao.xml
[NO MAP]

### allclub-interview/pom.xml
[NO MAP]

### allclub-oss/pom.xml
[NO MAP]

### allclub-oss/src/main/java/com/feng/oss/OssApplication.java
@SpringBootApplication
public class OssApplication
  - public static void main(String[] args)

### allclub-oss/src/main/java/com/feng/oss/adapter/AliStorageAdapter.java
public class AliStorageAdapter implements StorageAdapter
  - @Override
    public void createBucket(String bucket)
  - @Override
    public void uploadFile(MultipartFile uploadFile, String bucket, String objectName)
  - @Override
    public List<String> getAllBucket()
  - @Override
    public List<FileInfo> getAllFile(String bucket)
  - @Override
    public InputStream downLoad(String bucket, String objectName)
  - @Override
    public void deleteBucket(String bucket)
  - @Override
    public void deleteObject(String bucket, String objectName)
  - @Override
    public String getUrl(String bucket, String objectName)

### allclub-oss/src/main/java/com/feng/oss/adapter/MinioStorageAdapter.java
public class MinioStorageAdapter implements StorageAdapter
  - @Resource
    private MinioUtil minioUtil
  - @Value("${minio.url}")
    private String url
  - @Override
    @SneakyThrows //编译的时候加上throw Exception
    public void createBucket(String bucket)
  - @Override
    @SneakyThrows
    public void uploadFile(MultipartFile uploadFile, String bucket, String objectName)
  - @Override
    @SneakyThrows
    public List<String> getAllBucket()
  - @Override
    @SneakyThrows
    public List<FileInfo> getAllFile(String bucket)
  - @Override
    @SneakyThrows
    public InputStream downLoad(String bucket, String objectName)
  - @Override
    @SneakyThrows
    public void deleteBucket(String bucket)
  - @Override
    @SneakyThrows
    public void deleteObject(String bucket, String objectName)
  - @Override
    @SneakyThrows
    public String getUrl(String bucket, String objectName)

### allclub-oss/src/main/java/com/feng/oss/adapter/StorageAdapter.java
public interface StorageAdapter

### allclub-oss/src/main/java/com/feng/oss/config/MinioConfig.java
@Configuration
public class MinioConfig
  - @Value("${minio.url}")
    private String url
  - @Value("${minio.accessKey}")
    private String accessKey
  - @Value("${minio.secretKey}")
    private String accessSecret
  - @Bean("minioClient")
    public MinioClient getMinioClient()

### allclub-oss/src/main/java/com/feng/oss/config/StorageConfig.java
@Configuration
@RefreshScope
public class StorageConfig
  - @Value("${storage.service.type}")
    private String storageType
  - @Bean
    @RefreshScope
    public StorageAdapter storageService()

### allclub-oss/src/main/java/com/feng/oss/controller/FileController.java
@RestController
@RequestMapping("/oss")
public class FileController
  - @Resource
    private FileService fileService
  - @GetMapping("/testGetAllBuckets")
    public String testGetAllBuckets() throws Exception
  - @GetMapping("/getUrl")
    public String getUrl(String bucketName, String objectName) throws Exception
  - @RequestMapping("/upload")
    public Result<String> upload(MultipartFile uploadFile, String bucket, String objectName) throws Exception

### allclub-oss/src/main/java/com/feng/oss/entity/FileInfo.java
@Data
public class FileInfo
  - private String fileName
  - private Boolean directoryFlag
  - private String etag

### allclub-oss/src/main/java/com/feng/oss/entity/Result.java
@Data
public class Result<T>
  - private Boolean success
  - private Integer code
  - private String message
  - private T data
  - public static<T> Result<T> ok()
  - public static<T> Result<T> ok(T data)
  - public static<T> Result<T> fail()
  - public static <T> Result<T> fail(T data)

### allclub-oss/src/main/java/com/feng/oss/entity/ResultCodeEnum.java
@Getter
public enum ResultCodeEnum

### allclub-oss/src/main/java/com/feng/oss/service/FileService.java
@Service
public class FileService
  - private final StorageAdapter storageAdapter
  - public FileService(StorageAdapter storageAdapter)
  - public List<String> getAllBucket()
  - public String getUrl(String bucketName,String objectName)
  - public String uploadFile(MultipartFile uploadFile, String bucket, String objectName)

### allclub-oss/src/main/java/com/feng/oss/util/MinioUtil.java
@Component
public class MinioUtil
  - @Resource
    private MinioClient minioClient
  - public void createBucket(String bucketName) throws Exception
  - public void uploadFile(InputStream inputStream, String bucketName, String objectName) throws Exception
  - public List<String> getAllBucket() throws Exception
  - public List<FileInfo> getAllFile(String bucketName) throws Exception
  - public InputStream downLoad(String bucketName, String objectName) throws Exception
  - public void deleteBucket(String bucketName) throws Exception
  - public void deleteObject(String bucketName, String objectName) throws Exception
  - public String getPreviewFileUrl(String bucketName, String objectName) throws Exception

### allclub-oss/src/main/resources/application.yml
[NO MAP]

### allclub-oss/src/main/resources/bootstrap.yml
[NO MAP]

### allclub-practice/pom.xml
[NO MAP]

### allclub-practice/practice-api/pom.xml
[NO MAP]

### allclub-practice/practice-api/src/main/java/com/feng/practice/api/common/PageInfo.java
public class PageInfo
  - private Integer pageNo
  - private Integer pageSize
  - public Integer getPageNo()
  - public Integer getPageSize()

### allclub-practice/practice-api/src/main/java/com/feng/practice/api/common/PageResult.java
@Data
public class PageResult<T> implements Serializable
  - private Integer pageNo
  - private Integer pageSize
  - private Integer total
  - private Integer totalPages
  - private List<T> result
  - private Integer start
  - private Integer end
  - public void setRecords(List<T> result)
  - public void setTotal(Integer total)
  - public void setPageSize(Integer pageSize)
  - public void setPageNo(Integer pageNo)

### allclub-practice/practice-api/src/main/java/com/feng/practice/api/common/Result.java
@Data
public class Result<T>
  - private Boolean success
  - private Integer code
  - private String message
  - private T data
  - public static Result ok()
  - public static <T> Result ok(T data)
  - public static Result fail()
  - public static <T> Result fail(T data)

### allclub-practice/practice-api/src/main/java/com/feng/practice/api/common/ResultCodeEnum.java
@Getter
public enum ResultCodeEnum

### allclub-practice/practice-api/src/main/java/com/feng/practice/api/enums/AnswerStatusEnum.java
public enum AnswerStatusEnum

### allclub-practice/practice-api/src/main/java/com/feng/practice/api/enums/CompleteStatusEnum.java
public enum CompleteStatusEnum

### allclub-practice/practice-api/src/main/java/com/feng/practice/api/enums/IsDeletedFlagEnum.java
@Getter
public enum IsDeletedFlagEnum

### allclub-practice/practice-api/src/main/java/com/feng/practice/api/enums/SetTypeEnum.java
public enum SetTypeEnum

### allclub-practice/practice-api/src/main/java/com/feng/practice/api/enums/SubjectInfoTypeEnum.java
@Getter
public enum SubjectInfoTypeEnum

### allclub-practice/practice-api/src/main/java/com/feng/practice/api/req/GetPracticeSubjectListReq.java
@Data
public class GetPracticeSubjectListReq implements Serializable
  - private List<String> assembleIds

### allclub-practice/practice-api/src/main/java/com/feng/practice/api/req/GetPracticeSubjectReq.java
@Data
public class GetPracticeSubjectReq implements Serializable
  - private Long subjectId
  - private Integer subjectType

### allclub-practice/practice-api/src/main/java/com/feng/practice/api/req/GetPracticeSubjectsReq.java
@Data
public class GetPracticeSubjectsReq implements Serializable
  - private Long setId
  - private Long practiceId

### allclub-practice/practice-api/src/main/java/com/feng/practice/api/req/GetPreSetReq.java
@Data
public class GetPreSetReq implements Serializable
  - private Integer orderType
  - private PageInfo pageInfo
  - private String setName

### allclub-practice/practice-api/src/main/java/com/feng/practice/api/req/GetReportReq.java
@Data
public class GetReportReq implements Serializable
  - private Long practiceId

### allclub-practice/practice-api/src/main/java/com/feng/practice/api/req/GetScoreDetailReq.java
@Data
public class GetScoreDetailReq implements Serializable
  - private Long practiceId

### allclub-practice/practice-api/src/main/java/com/feng/practice/api/req/GetSubjectDetailReq.java
@Data
public class GetSubjectDetailReq implements Serializable
  - private Long practiceId
  - private Long subjectId
  - private Integer subjectType

### allclub-practice/practice-api/src/main/java/com/feng/practice/api/req/GetUnCompletePracticeReq.java
@Data
public class GetUnCompletePracticeReq implements Serializable
  - private PageInfo pageInfo

### allclub-practice/practice-api/src/main/java/com/feng/practice/api/req/SubmitPracticeDetailReq.java
@Data
public class SubmitPracticeDetailReq implements Serializable
  - private Long setId
  - private Long practiceId
  - private String timeUse
  - private String submitTime

### allclub-practice/practice-api/src/main/java/com/feng/practice/api/req/SubmitSubjectDetailReq.java
@Data
public class SubmitSubjectDetailReq implements Serializable
  - private Long practiceId
  - private Long subjectId
  - private List<Integer> answerContents
  - private Integer subjectType
  - private String timeUse

### allclub-practice/practice-api/src/main/java/com/feng/practice/api/req/package-info.md
[NO MAP]

### allclub-practice/practice-api/src/main/java/com/feng/practice/api/vo/PracticeSetVO.java
@Data
public class PracticeSetVO implements Serializable
  - private Long setId
  - private String setName
  - private Integer setHeat
  - private String setDesc

### allclub-practice/practice-api/src/main/java/com/feng/practice/api/vo/PracticeSubjectDetailVO.java
@Data
public class PracticeSubjectDetailVO implements Serializable
  - private Long subjectId
  - private Integer subjectType
  - private Integer isAnswer

### allclub-practice/practice-api/src/main/java/com/feng/practice/api/vo/PracticeSubjectListVO.java
@Data
public class PracticeSubjectListVO implements Serializable
  - private String title
  - private List<PracticeSubjectDetailVO> subjectList
  - private Long practiceId
  - private String timeUse

### allclub-practice/practice-api/src/main/java/com/feng/practice/api/vo/PracticeSubjectOptionVO.java
@Data
public class PracticeSubjectOptionVO implements Serializable
  - private Integer optionType
  - private String optionContent
  - private Integer isCorrect

### allclub-practice/practice-api/src/main/java/com/feng/practice/api/vo/PracticeSubjectVO.java
@Data
public class PracticeSubjectVO implements Serializable
  - private String subjectName
  - private Integer subjectType
  - private List<String> answerContentList
  - private List<PracticeSubjectOptionVO> optionList

### allclub-practice/practice-api/src/main/java/com/feng/practice/api/vo/RankVO.java
@Data
public class RankVO implements Serializable
  - private String avatar
  - private String name
  - private Integer count

### allclub-practice/practice-api/src/main/java/com/feng/practice/api/vo/ReportSkillVO.java
@Data
public class ReportSkillVO implements Serializable
  - private BigDecimal star
  - private String name

### allclub-practice/practice-api/src/main/java/com/feng/practice/api/vo/ReportVO.java
@Data
public class ReportVO implements Serializable
  - private String title
  - private String correctSubject
  - private List<ReportSkillVO> skill

### allclub-practice/practice-api/src/main/java/com/feng/practice/api/vo/ScoreDetailVO.java
@Data
public class ScoreDetailVO implements Serializable
  - private Long subjectId
  - private Integer subjectType
  - private Integer isCorrect

### allclub-practice/practice-api/src/main/java/com/feng/practice/api/vo/SpecialPracticeCategoryVO.java
@Data
public class SpecialPracticeCategoryVO implements Serializable
  - private String categoryName
  - private Long categoryId
  - private List<SpecialPracticeLabelVO> labelList

### allclub-practice/practice-api/src/main/java/com/feng/practice/api/vo/SpecialPracticeLabelVO.java
@Data
public class SpecialPracticeLabelVO implements Serializable
  - private Long id
  - private String assembleId
  - private String labelName

### allclub-practice/practice-api/src/main/java/com/feng/practice/api/vo/SpecialPracticeVO.java
@Data
public class SpecialPracticeVO implements Serializable
  - private String primaryCategoryName
  - private Long primaryCategoryId
  - private List<SpecialPracticeCategoryVO> categoryList

### allclub-practice/practice-api/src/main/java/com/feng/practice/api/vo/SubjectDetailVO.java
@Data
public class SubjectDetailVO implements Serializable
  - private List<Integer> correctAnswer
  - private List<Integer> respondAnswer
  - private String subjectParse
  - private List<PracticeSubjectOptionVO> optionList
  - private List<String> labelNames
  - private String subjectName

### allclub-practice/practice-api/src/main/java/com/feng/practice/api/vo/UnCompletePracticeSetVO.java
@Data
public class UnCompletePracticeSetVO implements Serializable
  - private Long setId
  - private Long practiceId
  - private String practiceTime
  - private String title

### allclub-practice/practice-server/pom.xml
[NO MAP]

### allclub-practice/practice-server/src/main/java/com/feng/practice/server/PracticeApplication.java
@SpringBootApplication
@ComponentScan("com.feng.practice")
@MapperScan("com.feng.**.dao")
@EnableFeignClients(basePackages = "com.feng")
public class PracticeApplication
  - public static void main(String[] args)

### allclub-practice/practice-server/src/main/java/com/feng/practice/server/config/GlobalConfig.java
@Configuration
public class GlobalConfig extends WebMvcConfigurationSupport
  - @Override
    protected void configureMessageConverters(List<HttpMessageConverter<?>> converters)
  - private MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter()

### allclub-practice/practice-server/src/main/java/com/feng/practice/server/config/context/UserContextHolder.java
public class UserContextHolder
  - private static final InheritableThreadLocal<Map<String, Object>> THREAD_LOCAL
  - public static void set(String key, Object val)
  - public static Object get(String key)
  - public static String getLoginId()
  - public static void remove()
  - public static Map<String, Object> getThreadLocalMap()

### allclub-practice/practice-server/src/main/java/com/feng/practice/server/config/interceptor/FeignConfiguration.java
@Configuration
public class FeignConfiguration
  - @Bean
    public RequestInterceptor requestInterceptor()

### allclub-practice/practice-server/src/main/java/com/feng/practice/server/config/interceptor/FeignRequestInterceptor.java
@Component
public class FeignRequestInterceptor implements RequestInterceptor
  - @Override
    public void apply(RequestTemplate requestTemplate)

### allclub-practice/practice-server/src/main/java/com/feng/practice/server/config/interceptor/LoginInterceptor.java
public class LoginInterceptor implements HandlerInterceptor
  - @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception
  - @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception

### allclub-practice/practice-server/src/main/java/com/feng/practice/server/config/mybatis/MybatisConfiguration.java
@Configuration
public class MybatisConfiguration
  - @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor()

### allclub-practice/practice-server/src/main/java/com/feng/practice/server/config/mybatis/MybatisPlusAllSqlLog.java
public class MybatisPlusAllSqlLog implements InnerInterceptor
  - public static final Logger log
  - @Override
    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) throws SQLException
  - @Override
    public void beforeUpdate(Executor executor, MappedStatement ms, Object parameter) throws SQLException
  - private static void logInfo(BoundSql boundSql, MappedStatement ms, Object parameter)
  - public static String getSql(Configuration configuration, BoundSql boundSql, String sqlId)
  - public static String showSql(Configuration configuration, BoundSql boundSql)
  - private static String getParameterValue(Object obj)

### allclub-practice/practice-server/src/main/java/com/feng/practice/server/config/mybatis/SqlStatementInterceptor.java
@Intercepts({
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class,
                Object.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class,
                Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class})})
public class SqlStatementInterceptor implements Interceptor
  - public static final Logger log
  - @Override
    public Object intercept(Invocation invocation) throws Throwable
  - @Override
    public Object plugin(Object target)
  - @Override
    public void setProperties(Properties properties)

### allclub-practice/practice-server/src/main/java/com/feng/practice/server/config/redis/RedisConfig.java
@Configuration
public class RedisConfig
  - @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory)
  - private Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer()

### allclub-practice/practice-server/src/main/java/com/feng/practice/server/config/redis/RedisUtil.java
@Component
@Slf4j
public class RedisUtil
  - @Resource
    private RedisTemplate redisTemplate
  - private static final String CACHE_KEY_SEPARATOR
  - public String buildKey(String... strObjs)
  - public boolean exist(String key)
  - public boolean del(String key)
  - public void set(String key, String value)
  - public boolean setNx(String key, String value, Long time, TimeUnit timeUnit)
  - public String get(String key)
  - public Boolean zAdd(String key, String value, Long score)
  - public Long countZset(String key)
  - public Set<String> rangeZset(String key, long start, long end)
  - public Long removeZset(String key, Object value)
  - public void removeZsetList(String key, Set<String> value)
  - public Double score(String key, Object value)
  - public Set<String> rangeByScore(String key, long start, long end)
  - public Object addScore(String key, Object obj, double score)
  - public Object rank(String key, Object obj)

### allclub-practice/practice-server/src/main/java/com/feng/practice/server/controller/DemoController.java
@RestController
@RequestMapping("/practice/")
@Slf4j
public class DemoController
  - @RequestMapping("test")
    public String test()

### allclub-practice/practice-server/src/main/java/com/feng/practice/server/controller/PracticeDetailController.java
@RestController
@Slf4j
@RequestMapping("/practice/detail")
public class PracticeDetailController
  - @Resource
    private PracticeDetailService practiceDetailService
  - @Resource
    private PracticeSetService practiceSetService
  - @PostMapping(value = "/submit")
    public Result<Boolean> submit(@RequestBody SubmitPracticeDetailReq req)
  - @PostMapping(value = "/submitSubject")
    public Result<Boolean> submitSubject(@RequestBody SubmitSubjectDetailReq req)
  - @PostMapping(value = "/getScoreDetail")
    public Result<List<ScoreDetailVO>> getScoreDetail(@RequestBody GetScoreDetailReq req)
  - @PostMapping(value = "/getSubjectDetail")
    public Result<SubjectDetailVO> getSubjectDetail(@RequestBody GetSubjectDetailReq req)
  - @PostMapping(value = "/getReport")
    public Result<ReportVO> getReport(@RequestBody GetReportReq req)
  - @PostMapping(value = "/getPracticeRankList")
    public Result<List<RankVO>> getPracticeRankList()
  - @PostMapping(value = "/giveUp")
    public Result<Boolean> giveUp(@RequestParam("practiceId") Long practiceId)

### allclub-practice/practice-server/src/main/java/com/feng/practice/server/controller/PracticeSetController.java
@RestController
@RequestMapping("/practice/set")
@Slf4j
public class PracticeSetController
  - @Resource
    private PracticeSetService practiceSetService
  - @GetMapping("/getSpecialPracticeContent")
    public Result<List<SpecialPracticeVO>> getSpecialPracticeContent()
  - @PostMapping(value = "/addPractice")
    public Result<PracticeSetVO> addPractice(@RequestBody GetPracticeSubjectListReq req)
  - @PostMapping(value = "/getSubjects")
    public Result<PracticeSubjectListVO> getSubjects(@RequestBody GetPracticeSubjectsReq req)
  - @PostMapping(value = "/getPracticeSubject")
    public Result<PracticeSubjectVO> getPracticeSubject(@RequestBody GetPracticeSubjectReq req)
  - @PostMapping(value = "/getPreSetContent")
    public Result<PageResult<PracticeSetVO>> getPreSetContent(@RequestBody GetPreSetReq req)
  - @PostMapping(value = "/getUnCompletePractice")
    public Result<PageResult<UnCompletePracticeSetVO>> getUnCompletePractice(@RequestBody GetUnCompletePracticeReq req)

### allclub-practice/practice-server/src/main/java/com/feng/practice/server/dao/PracticeDao.java
public interface PracticeDao

### allclub-practice/practice-server/src/main/java/com/feng/practice/server/dao/PracticeDetailDao.java
public interface PracticeDetailDao

### allclub-practice/practice-server/src/main/java/com/feng/practice/server/dao/PracticeSetDao.java
public interface PracticeSetDao

### allclub-practice/practice-server/src/main/java/com/feng/practice/server/dao/PracticeSetDetailDao.java
public interface PracticeSetDetailDao

### allclub-practice/practice-server/src/main/java/com/feng/practice/server/dao/SubjectCategoryDao.java
public interface SubjectCategoryDao

### allclub-practice/practice-server/src/main/java/com/feng/practice/server/dao/SubjectDao.java
public interface SubjectDao

### allclub-practice/practice-server/src/main/java/com/feng/practice/server/dao/SubjectJudgeDao.java
public interface SubjectJudgeDao

### allclub-practice/practice-server/src/main/java/com/feng/practice/server/dao/SubjectLabelDao.java
public interface SubjectLabelDao

### allclub-practice/practice-server/src/main/java/com/feng/practice/server/dao/SubjectMappingDao.java
public interface SubjectMappingDao

### allclub-practice/practice-server/src/main/java/com/feng/practice/server/dao/SubjectMultipleDao.java
public interface SubjectMultipleDao

### allclub-practice/practice-server/src/main/java/com/feng/practice/server/dao/SubjectRadioDao.java
public interface SubjectRadioDao

### allclub-practice/practice-server/src/main/java/com/feng/practice/server/entity/dto/CategoryDTO.java
@Data
public class CategoryDTO
  - private List<Integer> subjectTypeList
  - private Integer categoryType
  - private Long parentId

### allclub-practice/practice-server/src/main/java/com/feng/practice/server/entity/dto/PracticeSetDTO.java
@Data
public class PracticeSetDTO implements Serializable
  - private List<Long> excludeSetId
  - private Integer setType
  - private Long primaryCategoryId
  - private Integer limitCount
  - private Integer orderType
  - private String setName
  - private PageInfo pageInfo

### allclub-practice/practice-server/src/main/java/com/feng/practice/server/entity/dto/PracticeSubjectDTO.java
@Data
public class PracticeSubjectDTO implements Serializable
  - private List<String> assembleIds
  - private Integer subjectType
  - private Integer subjectCount
  - private List<Long> excludeSubjectIds
  - private Long subjectId

### allclub-practice/practice-server/src/main/java/com/feng/practice/server/entity/dto/SubjectDTO.java
@Data
public class SubjectDTO implements Serializable
  - private Long id
  - private Long subjectId
  - private String subjectName
  - private Integer subjectType

### allclub-practice/practice-server/src/main/java/com/feng/practice/server/entity/dto/SubjectDetailDTO.java
@Data
public class SubjectDetailDTO implements Serializable
  - private Long id
  - private String subjectName
  - private Integer isCorrect
  - private String subjectParse
  - private List<SubjectOptionDTO> optionList

### all