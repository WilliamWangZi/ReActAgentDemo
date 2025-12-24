package com.example.reactagentdemo.infra;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.dashscope.QwenEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.chroma.ChromaEmbeddingStore;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class LangChain4jChromaQwenRAG {

    // 替换为你的真实配置
    private static final String CHROMA_BASE_URL = "http://localhost:8000";
    private static final String COLLECTION_NAME = "verified-qwen-rag";
    private static final String QWEN_EMBED_MODEL = "text-embedding-v4";

    // 核心组件
    private final QwenEmbeddingModel qwenEmbeddingModel;
    private final ChromaEmbeddingStore embeddingStore;
    private final EmbeddingStoreIngestor ingestor;

    public LangChain4jChromaQwenRAG() {
        // 1. 初始化千问嵌入模型
        String apiKey = System.getenv("DASHSCOPE_API_KEY");
        this.qwenEmbeddingModel = QwenEmbeddingModel.builder()
                .apiKey(apiKey)
                .modelName(QWEN_EMBED_MODEL)
                .build();

        // 2. 初始化 Chroma 存储（0.32.0 版本仅支持 baseUrl + collectionName）
        this.embeddingStore = ChromaEmbeddingStore.builder()
                .baseUrl(CHROMA_BASE_URL)
                .collectionName(COLLECTION_NAME)
                .build();

        // 3. 初始化摄入器
        this.ingestor = EmbeddingStoreIngestor.builder()
                .embeddingModel(qwenEmbeddingModel)
                .embeddingStore(embeddingStore)
                .build();
    }

    // 新增文档
    public void addDocument(String content, Metadata metadata) {
        ingestor.ingest(Document.from(content, metadata));
    }

    // 检索文档
    public List<EmbeddingMatch<TextSegment>> retrieve(String query, int topK) {
        Embedding queryEmbedding = qwenEmbeddingModel.embed(query).content();
        return embeddingStore.findRelevant(queryEmbedding, topK);
    }

    // ✅ 直接调用 remove 方法（0.35.0+ 支持）
    public void removeDocument(String documentId) {
        embeddingStore.remove(documentId); // 核心：使用 remove 方法
        System.out.println("成功移除文档 ID：" + documentId);
    }


    // 更新文档（remove + add）
    public void updateDocument(String oldContent, String newContent, Metadata newMeta) {
        // 1. 找到旧文档 ID
        Embedding oldEmbedding = qwenEmbeddingModel.embed(oldContent).content();
        List<EmbeddingMatch<TextSegment>> oldMatches = embeddingStore.findRelevant(oldEmbedding, 1);

        if (!oldMatches.isEmpty()) {
            String oldId = oldMatches.get(0).embeddingId();
            embeddingStore.remove(oldId); // 用 remove 删除旧文档
        }
        // 2. 新增新文档
        addDocument(newContent, newMeta);
    }

    public static void main(String[] args) {
        LangChain4jChromaQwenRAG rag = new LangChain4jChromaQwenRAG();
        String text = "王子鹏\n" +
               "所在地：广东深圳    \n" +
               "联系电话：13610173128\n" +
               "工作年限：9 年\n" +
               "联系邮箱：674416774@qq.com\n" +
               "个人优势\n" +
               "9 年 Java 后端经验，深耕 B 端业务，多次微服务拆分与系统重构经验，制定团队技术规范、把控项目质量；解决数据质量与资损问题，降本提效显著，兼具架构设计与团队项目管理能力\n" +
               "工作经历\n" +
               "\n" +
               "---\n" +
               "\n" +
               "字节跳动-预算部门 \n" +
               "资深 java开发\n" +
               "2024.7 - 至今\n" +
               "- 领域划分与架构治理：作为预算申请方向负责人，主导review系统架构风险问题，主导多次架构评审，识别多项架构风险（如服务依赖，大事务），产出后续系统技术治理规划；划分预算领域边界，负责预算系统微服务拆分工作\n" +
               "- 团队管理与技术规范：规范大团队研发双周迭代流程，负责3人小团队的日常研发管理与技术评审工作，制定团队技术规范（日志，接口，异常，微服务调用，数据库等），优化工程分层结构，统一标准技术组件\n" +
               "- 核心平台建设：牵头搭建sql刷数运营平台，制定数据执行规范，杜绝研发生产随意刷数的安全风险；产出统一配置中心平台，降低配置开发成本，释放研发人力 38d/年\n" +
               "\n" +
               "字节跳动-采购供应商部门\n" +
               "高级 java开发\n" +
               "2019.11 - 2024.6\n" +
               "- 微服务架构拆分与系统重构：参与供应商领域边界与微服务划分建设，拆分大单体服务，主导供应商主数据与绩效子系统架构设计和重构工作，降低系统耦合度 80%，系统部署效率提升 xx%\n" +
               "- 数据库迁移与性能优化：按领域拆分迁移供应商表到独立数据库，解决数据架构合理性，拓展性问题，研发成本降低 xx %；治理慢SQL条，引入 Elasticsearch 大宽表优化复杂查询，杜绝所有1秒以上慢sql\n" +
               "- 资损防控与业务价值：梳理供应商资损风险地图，建设供应商资损防控体系，24年拦截 15+ 潜在资损问题；治理供应商信息质量问题，超2w+供应商信息得到纠正，降低 9.57M USD/年 资损风险\n" +
               "\n" +
               "金蝶-我家云网络科技有限公司\n" +
               " java开发\n" +
               "2016.7 - 2019.10\n" +
               "- 核心业务模块开发与业务支撑：负责我家云物业平台核心模块（云租赁、云收费、云设备）开发，支撑平台年流水超 20 亿元，服务 300 个小区、数百万用户；保障融创标杆项目近百个小区设备模块零故障运行，维护数百万条任务数据\n" +
               "- 查询与导出性能优化：部署solr搜索引擎提升平台性能，优化基础业务搜索；设计大文件异步导出方案（基于 异步导出 + 任务表），解决大数据导出超时问题\n" +
               "- 小团队管理与技术沉淀：带领 3 人小团队完成基础业务开发，负责后端模块表设计，编写设计文档，接口文档；优化日志排查和研发过程热部署，提升研发效率\n" +
               "\n" +
               "项目经历\n" +
               "\n" +
               "---\n" +
               "立项平台\n" +
               "\n" +
               "2024.7 - 至今\n" +
               "描述\n" +
               "- 作为集团统一的“预算费用”管理工具，为各部门的业务及财务，提供项目费用从“事前决策”、“事中管控”到“事后复盘”的闭环管理服务\n" +
               "核心技术栈\n" +
               "- 微服务架构、SSM 框架、MySQL、MQ、Redis、ES、RocketMQ、网关\n" +
               "关键挑战与产出\n" +
               "- 微服务治理：划分预算系统内子功能边界，review立项平台架构设计与稳定性问题，输出技术治理方向规划\n" +
               "- 团队研发标准不统一：制定工程规范，开发技术规范，研发流程规范，统一研发认知提升效率\n" +
               "- 系统业务配置扩展性差：基于低代码页面+统一业务配置表搭建统一的业务配置平台，收敛分散配置功能\n" +
               "- 生产刷数稳定性风险：搭建低代码刷数审批运营平台，制定刷数运营规范\n" +
               "\n" +
               "供应商平台\n" +
               "\n" +
               "2019.11 - 2024.6\n" +
               "描述\n" +
               "- 公司全球统一的供应商管理工具，用于管理涉及「支出」（包含既收又支）供应商\n" +
               "核心技术栈：\n" +
               "- 微服务架构、SSM 框架、MySQL、MQ、Redis、ES、RocketMQ、网关\n" +
               "关键挑战与产出\n" +
               "- 治理供应商数据重复问题：纠正供应商信息双系统冗余存储架构，设计 “数据清洗 + 唯一性识别校验机制（基于身份证 / 企业码）” 方案，另外引入数据监控观察增量数据问题\n" +
               "- 数据库领域耦合迁移：负责 110 + 张表从单体库到供应商独立数据库的迁移项目，采用 “全量copy + 增量binlog+多工具验证数据一致性” 方案，零停机完成迁移\n" +
               "- 解决大单体系统问题：供应商大单体服务拆分为5个微服务，服务间采用openFeign通信，主导主数据和绩效两个子域重构\n" +
               "- 资损风险收敛：梳理具体不同业务风险场景，按场景搭建 “事前预防 + 事中拦截 + 事后预案” 的资损防控体系，杜绝资损发生\n" +
               "- 性能优化：引入ES，采用 binlog + mq 实时同步数据库更新到ES集群数据，构建ES大宽表，解决mysql多表复杂查询问题\n" +
               "\n" +
               "我家云物业平台\n" +
               "\n" +
               "2016.7 - 2019.10\n" +
               "描述：\n" +
               "- 我家云是一个物业服务平台，为物业服务提供一系列解决方案，提高物业运营管理效率和服务品质\n" +
               "核心技术栈：\n" +
               "- Java（JDK8）、SSM、MySQL（读写分离）、Redis、ActiveMQ、Solr、Nginx\n" +
               "关键挑战与产出：\n" +
               "- 模糊慢查询问题解决：引入 Solr 搜索引擎优化模糊查询，搭配数据库索引，提升核心模块查询速度，减轻数据库压力\n" +
               "- 大数据导出超时方案：设计大文件异步导出方案（基于 异步导出 + 任务表），解决大数据导出超时问题\n" +
               "- 业务配置统一：设计基础业务参数配置方案，缩短参数功能开发周期，降低迭代成本\n" +
               "- 研发效率提升：引入日志请求链与 MyBatis 热部署，提升问题排查效率与研发迭代速度\n" +
               "\n" +
               "教育经历\n" +
               "\n" +
               "---\n" +
               "广东工业大学 — 物联网工程  —  本科 — 2012-2016\n" +
               "- 2015 年获广东工业大学优秀学生一等奖，班排第一\n" +
               "\n" +
               "技术栈与证书\n" +
               "- 核心技术：Java、Spring Boot、MySQL、Mybatis、Redis、RocketMQ、Elasticsearch\n" +
               "- 架构：微服务架构、分布式系统、高可用设计、性能优化\n" +
               "- 证书：软考 - 程序员证、CET-4\n";
        String[] textArr = text.split("\n");
        // 1. 新增文档
        for (String s : textArr) {
            if (StringUtils.isNotBlank(s)) {
                rag.addDocument(s, Metadata.from("type", "test"));
            }
        }

        // 3. 检索并移除文档
        List<EmbeddingMatch<TextSegment>> matches = rag.retrieve("王子鹏的工作经验多久", 5);
        System.out.println(matches.stream()
                .map(match -> match.embedded().text())
                .collect(Collectors.toList()));
//        rag.removeDocument(matches.get(0).embeddingId());

    }
}
