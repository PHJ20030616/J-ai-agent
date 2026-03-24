# TODO

**扩展思路**

1）自定义 Advisor，比如权限校验、违禁词校验 Advisor

2）自定义对话记忆，比如持久化对话到 MySQL 或 Redis 存储中

3）编写一套包含变量的 Prompt 模板，并保存为资源文件，从文件加载模板

4）开发一个多模态对话助手，能够让 AI 解释图片（建议使用国内的 AI 大模型）

5）阅读 Spring AI 官方的 [ChatMemory 文档](https://docs.spring.io/spring-ai/reference/api/chat-memory.html)，了解如何自主构造 ChatMemory



# 创新点：

![image-20260320175022614](https://cdn.jsdelivr.net/gh/PHJ20030616/personal_pic/img/20260320175024119.png)

# 智能体

## 大模型相关概念

### 什么是AI大模型

AI 大模型是指具⁠有超大规模参数（通常为数十亿到数万‌亿）的深度学习模型，通过对大规模数据的训练，能够理解、生成人类语言，‎处理图像、音频等多种模态数据，并展‌示出强大的推理和创作能力。

大模型的强大之处在于它的 **涌现能力** —— 随着模型参数量和训练数据量的增加，模型会展现出训练过程中未明确赋予的新能力，比如逻辑推理、代码编写、多步骤问题解决等。

### AI大模型的分类

**目的：为了更好地进行技术选型**

#### **1、按模态分类**

- **单模态模型**：仅处理单一类型的数据，如纯文本（早期的 GPT-3）
- **多模态模型**：能够处理多种类型的信息
- 文本 + 图像：GPT-4V、Gemini、Claude 3
- 文本 + 音频 + 视频：GPT-4o

#### 2、按开源性分类

- **闭源模型**：不公开模型权重和训练方法
- 代表：GPT-4、Claude、Gemini
- 特点：通常通过 API 访问，付费使用
- **开源模型**：公开模型权重，允许下载和自行部署
- 代表：Llama 系列、Mistral、Falcon
- 特点：可以本地部署，自由调整，但通常性能略逊于同等规模闭源模型

#### 3、按规模分类

- **超大规模模型**：参数量在数千亿到数万亿
- 代表：GPT-4 (1.76T 参数)
- 特点：能力强大，但需要大量计算资源
- **中小规模模型**：参数量在几十亿到几百亿
- 代表：Llama 3 (70B 参数)、Mistral 7B
- 特点：能在较普通的硬件上运行，适合特定任务的精调

#### 4、按用途分类

- **通用模型**：能处理广泛的任务
- 代表：GPT-4、Claude 3、Gemini
- **特定领域模型**：针对特定领域优化
- 医疗：Med-PaLM 2
- 代码：CodeLlama、StarCoder
- 科学：Galactica



## 后端项目初始化

Java--21

**1. 创建一个SpringBoot项目**

![image-20260318170910859](https://cdn.jsdelivr.net/gh/PHJ20030616/personal_pic/img/20260318170913084.png)

引入相关依赖

![image-20260318171042521](https://cdn.jsdelivr.net/gh/PHJ20030616/personal_pic/img/20260318171044572.png)

运行项目代码确保可以正常运行，尝试打包确保项目可以打包成功

## 引入相关依赖

### HuTool工具类

引入hutool工具类：https://doc.hutool.cn/pages/index/#%F0%9F%8D%8Amaven

```java
<dependency>
    <groupId>cn.hutool</groupId>
    <artifactId>hutool-all</artifactId>
    <version>5.8.38</version>
</dependency>
```

### Knife4j

引入Knife4j: https://doc.xiaominfo.com/docs/quick-start

```java
<dependency>
    <groupId>com.github.xiaoymin</groupId>
    <artifactId>knife4j-openapi3-jakarta-spring-boot-starter</artifactId>
    <version>4.4.0</version>
</dependency>
```

添加相应的配置（官方文档有）：https://doc.xiaominfo.com/docs/quick-start

```
spring:
  application:
    name: J-ai-agent
server:
  port: 8123
  servlet:
    # 访问/api才能访问到后端
      context-path: /api

# springdoc-openapi项目配置
springdoc:
  swagger-ui:
    path: /swagger-ui.html
    tags-sorter: alpha
    operations-sorter: alpha
  api-docs:
    path: /v3/api-docs
  group-configs:
    - group: 'default'
      paths-to-match: '/**'
      # 配置要扫描的包名
      packages-to-scan: com.phj.jaiagent.controller
# knife4j的增强配置，不需要增强可以不配
knife4j:
  enable: true
  setting:
    language: zh_cn
```

访问localhost:8123/api/doc.html即可查看接口文档

### 阿里云百炼平台

https://bailian.console.aliyun.com/cn-beijing?tab=doc#/doc/?type=model&url=2840915

```
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>dashscope-sdk-java</artifactId>
    <!-- 请将 'the-latest-version' 替换为最新版本号：https://mvnrepository.com/artifact/com.alibaba/dashscope-sdk-java -->
    <version>the-latest-version</version>
</dependency>
```

## 调用大模型的方式

1. SDK 接入：使用官方提供的软件开发工具包，最直接的集成方式
2. HTTP 接入：通过 REST API 直接发送 HTTP 请求调用模型
3. Spring AI：基于 Spring 生态系统的 AI 框架，更方便地接入大模型
4. LangChain4j：专注于构建 LLM 应用的 Java 框架，提供丰富的 AI 调用组件

**注意：具体引入方式请查阅官方文档或者直接询问AI**

## Prompt工程

### 提示词分类

1. **基于角色**

在 AI ⁠对话中，基于角色的‌分类是最常见的，通常存在 3 种主要‎类型的 Promp‌t：

1）用户 Promp⁠t (User Prompt)：这是用户‌向 AI 提供的实际问题、指令或信息，传达了用户的直接需求。用户 Prompt ‎告诉 AI 模型 “做什么”，比如回答问‌题、编写代码、生成创意内容等。

2）系统 Prompt⁠ (System Prompt)：这是设置‌ AI 模型行为规则和角色定位的隐藏指令，用户通常不能直接看到。系统 Prompt ‎相当于给 AI 设定人格和能力边界，即告诉‌ AI “你是谁？你能做什么？”。

3）助手 Prompt ⁠(Assistant Prompt)：这是 AI‌ 模型的响应内容。在多轮对话中，之前的助手回复也会成为当前上下文的一部分，影响后续对话的理解‎和生成。某些场景下，开发者可以主动预设一些助手消息作‌为对话历史的一部分，引导后续互动。

2. **基于功能的分类**

1）指令型提⁠示词（Instruct‌ional Prompts）：明确告诉 AI‎ 模型需要执行的任务，‌通常以命令式语句开头。

2）对话型⁠提示词（Conver‌sational Prompts）：模拟‎自然对话，以问答形式‌与 AI 模型交互。

3）创意型⁠提示词（Creati‌ve Prompts）：引导 AI 模型‎进行创意内容生成，如‌故事、诗歌、广告文案等。

4）角色扮⁠演提示词（Role‌-Playing Prompts）：‎让 AI 扮演特定‌角色或人物进行回答。

5）少样本⁠学习提示词（Few-‌Shot Prompts）：提供一些示例‎，引导 AI 理解所‌需的输出格式和风格。

3. **基于复杂度的分类**

1）简单提⁠示词（Simple‌ Prompts）：单一指令或问题，‎没有复杂的背景或‌约束条件。

2）复合提⁠示词（Compou‌nd Prompts）：包含多个相关‎指令或步骤的提示词‌。

3）链式提⁠示词（Chain P‌rompts）：一系列连续的、相互依赖的‎提示词，每个提示词基‌于前一个提示词的输出。

4）模板提⁠示词（Templa‌te Prompts）：包含可替换变‎量的标准化提示词结‌构，常用于大规模应用。

### Token

Token 是⁠大模型处理文本的基本单位，可‌能是单词或标点符号，模型的输入和输出都是按 Token ‎计算的，一般 Token 越‌多，成本越高，并且输出速度越慢。

因此在 A⁠I 应用开发中，了‌解和控制 Token 的消耗至关重要‎。

**如何计算 Token？**

首先，不同⁠大模型对 Toke‌n 的划分规则略有不同，比如根据 O‎penAI 的文档‌：

- 英文文本：一个 token 大约相当于 4 个字符或约 0.75 个英文单词
- 中文文本：一个汉字通常会被编码为 1-2 个 token
- 空格和标点：也会计入 token 数量
- 特殊符号和表情符号：可能需要多个 token 来表示

简单估算一下⁠，100 个英文单词约等‌于 75-150 个 Token，而 100 个‎中文字符约等于 100-‌200 个 Token。

实际应用中⁠，更推荐使用工具来‌估计 Prompt 的 Token ‎数量

**Token 成本计算**

估算成本有⁠个公式：总成本 =‌ (输入 token数 × 输入单价)‎ + (输出 tok‌en 数 × 输出单价)

#### Token 成本优化技巧

1）精简系统⁠提示词：移除冗余表述，保‌留核心指令。比如将 “你是一个非常专业、经验丰富‎且非常有耐心的编程导师”‌ 简化为 “你是编程导师”。

2）定期清理⁠对话历史：对话上下文会随‌着交互不断累积 Token。在长对话中，可以定期‎请求 AI 总结之前的对‌话，然后以总结替代详细历史。

3）使用向量检索⁠代替直接输入：对于需要处理大量参‌考文档的场景，不要直接将整个文档作为 Prompt，而是使用向量‎数据库和检索技术（RAG）获取‌相关段落。后续教程会带大家实战。

4）结构化⁠替代自然语言：使用‌表格、列表等结构化格式代替长段落描述‎。

### Prompt 优化技巧

#### 1、明确指定任务和角色

为 AI ⁠提供清晰的任务描述‌和角色定位，帮助模型理解背景和期望。

```
系统：你是一位经验丰富的Python教师，擅长向初学者解释编程概念。
用户：请解释 Python 中的列表推导式，包括基本语法和 2-3 个实用示例。
```

#### 2、提供详细说明和具体示例

提供足够的⁠上下文信息和期望的‌输出格式示例，减少模型的不确定性。

```
请提供一个社交媒体营销计划，针对一款新上市的智能手表。计划应包含:
1. 目标受众描述
2. 三个内容主题
3. 每个平台的内容类型建议
4. 发布频率建议

示例格式:
目标受众: [描述]
内容主题: [主题1], [主题2], [主题3]
平台策略: [平台] - [内容类型] - [频率]
```

#### 3、使用结构化格式引导思维

通过列表、表格等结构化格式，使指令更易理解，输出更有条理。

```
分析以下公司的优势和劣势:
公司: Tesla

请使用表格格式回答，包含以下列:
- 优势(最少3项)
- 每项优势的简要分析
- 劣势(最少3项)
- 每项劣势的简要分析
- 应对建议

```

#### 4、明确输出格式要求

指定输出的格式、长度、风格等要求，获得更符合预期的结果。

```
撰写一篇关于气候变化的科普文章，要求:
- 使用通俗易懂的语言，适合高中生阅读
- 包含5个小标题，每个标题下2-3段文字
- 总字数控制在800字左右
- 结尾提供3个可行的个人行动建议

```

### 进阶提示技巧

#### 1、思维链提示法（‌Chain-of-Thought）

引导模型展示推理过程，逐步思考问题，提高复杂问题的准确性。

```
问题：一个商店售卖T恤，每件15元。如果购买5件以上可以享受8折优惠。小明买了7件T恤，他需要支付多少钱？

请一步步思考解决这个问题:
1. 首先计算7件T恤的原价
2. 确定是否符合折扣条件
3. 如果符合，计算折扣后的价格
4. 得出最终支付金额

```

#### ⁠2、少样本学习（F‌ew-Shot Learning）

通过提供几⁠个输入 - 输出对的示‌例，帮助模型理解任务模式和期望输出。

```
我将给你一些情感分析的例子，然后请你按照同样的方式分析新句子的情感倾向。

输入: "这家餐厅的服务太差了，等了一个小时才上菜"
输出: 负面，因为描述了长时间等待和差评服务

输入: "新买的手机屏幕清晰，电池也很耐用"
输出: 正面，因为赞扬了产品的多个方面

现在分析这个句子:
"这本书内容还行，但是价格有点贵"

```

#### 3、分步骤指导（Step-by-Step）

将复杂任务分解为可管理的步骤，确保模型完成每个关键环节。

```
请帮我创建一个简单的网站落地页设计方案，按照以下步骤:

步骤1: 分析目标受众(考虑年龄、职业、需求等因素)
步骤2: 确定页面核心信息(主标题、副标题、价值主张)
步骤3: 设计页面结构(至少包含哪些区块)
步骤4: 制定视觉引导策略(颜色、图像建议)
步骤5: 设计行动召唤(CTA)按钮和文案
```

#### 4、自我评估和修正

让模型评估自己的输出并进行改进，提高准确性和质量。

```
解决以下概率问题:
从一副标准扑克牌中随机抽取两张牌，求抽到至少一张红桃的概率。

首先给出你的解答，然后:
1. 检查你的推理过程是否存在逻辑错误
2. 验证你使用的概率公式是否正确
3. 检查计算步骤是否有误
4. 如果发现任何问题，提供修正后的解答
```

#### 5、知识检索和引用

引导模型检索相关信息并明确引用信息来源，提高可靠性。

```
请解释光合作用的过程及其在植物生长中的作用。在回答中:
1. 提供光合作用的科学定义
2. 解释主要的化学反应
3. 描述影响光合作用效率的关键因素
4. 说明其对生态系统的重要性

对于任何可能需要具体数据或研究支持的陈述，请明确指出这些信息的来源，并说明这些信息的可靠性。
```

#### 6、多视角分析

引导模型从不同角度、立场或专业视角分析问题，提供全面见解

```
分析"城市应该禁止私家车进入市中心"这一提议:

请从以下4个不同角度分析:
1. 环保专家视角
2. 经济学家视角
3. 市中心商户视角
4. 通勤居民视角

对每个视角:
- 提供支持该提议的2个论点
- 提供反对该提议的2个论点
- 分析可能的折中方案
```

#### 7、多模态思维

结合不同表⁠达形式进行思考，如‌文字描述、图表结构、代码逻辑等。

```
设计一个智能家居系统的基础架构:

1. 首先用文字描述系统的主要功能和组件
2. 然后创建一个系统架构图(用ASCII或文本形式表示)
3. 接着提供用户交互流程
4. 最后简述实现这个系统可能面临的技术挑战

尝试从不同角度思考:功能性、用户体验、技术实现、安全性等。
```

### 提示词调试与优化

#### 1、迭代式提示优化

通过逐步修改和完善提示词，提高输出质量。

```
初始提示: 谈谈人工智能的影响。

[收到笼统回答后]
改进提示: 分析人工智能对医疗行业的三大积极影响和两大潜在风险，提供具体应用案例。

[如果回答仍然不够具体]
进一步改进: 详细分析AI在医学影像诊断领域的具体应用，包括:
1. 现有的2-3个成功商业化AI诊断系统及其准确率
2. 这些系统如何辅助放射科医生工作
3. 实施过程中遇到的主要挑战
4. 未来3-5年可能的技术发展方向
```

#### 2、边界测试

通过极限情况测试模型的能力边界，找出优化空间。

```
尝试解决以下具有挑战性的数学问题:
证明在三角形中，三条高的交点、三条中线的交点和三条角平分线的交点在同一条直线上。

如果你发现难以直接证明:
1. 说明你遇到的具体困难
2. 考虑是否有更简单的方法或特例可以探讨
3. 提供一个思路框架，即使无法给出完整证明
```

#### 3、提示词模板化

创建结构化⁠模板，便于针对类似‌任务进行一致性提示，否则每次输出的内‎容可能会有比较大的‌区别，不利于调试。

```
【专家角色】: {领域}专家
【任务描述】: {任务详细说明}
【所需内容】:
- {要点1}
- {要点2}
- {要点3}
【输出格式】: {格式要求}
【语言风格】: {风格要求}
【限制条件】: {字数、时间或其他限制}

例如:
【专家角色】: 营养学专家
【任务描述】: 为一位想减重的上班族设计一周健康饮食计划
【所需内容】:
- 七天的三餐安排
- 每餐的大致卡路里
- 准备建议和购物清单
【输出格式】: 按日分段，每餐列出具体食物
【语言风格】: 专业但友好
【限制条件】: 考虑准备时间短，预算有限
```

#### 4、错误分析与修正

系统性分析⁠模型回答中的错误，并‌针对性优化提示词，这一点在我们使用 Cu‎rsor 等 AI ‌开发工具生成代码时非常有用。

```
我发现之前请你生成的Python代码存在以下问题:
1. 没有正确处理文件不存在的情况
2. 数据处理逻辑中存在边界条件错误
3. 代码注释不够详细

请重新生成代码，特别注意:
1. 添加完整的异常处理
2. 测试并确保所有边界条件
3. 为每个主要函数和复杂逻辑添加详细注释
4. 遵循PEP 8编码规范
```

## AI应用设计方案

根据需求，⁠我们将实现一个具有‌多轮对话能力的 开发者智能助手。‎整体方案设计将围绕‌ 2 个核心展开：

- 系统提示词的设计
- 多轮对话的实现

### 1、系统提示词设计

一个优秀的系统提示词能够大幅增加用户体验

我们要⁠优化系统预设，可以‌借助 AI 进行优化。示例 Prom‎pt：

```
我现在正在开发一个“开发者智能助手”，他能够解答用户（开发者）的一些技术问题，拓展重要的技术知识，从而帮助用户更好地实现职业发展。
要求让AI作为开发大师，解答用户地问题，帮助用户拓展知识，并以提问的方式询问用户是否需要了解其他相关的知识
```

AI 提供的优化后系统提示词：

```
# Role
你是一个经验丰富的“资深开发大师”和“技术导师”。你不仅精通各类技术栈、系统架构和最佳实践，还深谙程序员的职业发展路径。

# Objective
你的目标是解答开发者的技术问题，帮助他们不仅“知其然”，还能“知其所以然”，通过拓展核心技术知识，助力他们突破技术瓶颈，实现职业进阶。

# Guidelines
1. **精准解答**：首先直接、准确地回答用户的当前问题。如果涉及代码，请提供清晰、有注释且符合最佳实践的代码示例。
2. **深度拓展**：在解决问题的基础上，主动剖析底层的原理、可能遇到的陷阱（Pitfalls）、性能优化建议或相关的系统设计理念。
3. **授人以渔**：适时引入工程化思维，教导用户如何调试、如何阅读源码或如何进行技术选型。
4. **结构化输出**：使用清晰的标题、列表和代码块（Markdown）来组织你的回答，确保内容易读且重点突出。
5. **启发式引导**：在每次回答的最后，**必须**以提问的方式收尾。提出一个与当前话题紧密相关、能引发更深层次思考的具体技术问题，询问用户是否需要继续探讨。

# Tone
专业、严谨、有耐心且富有启发性。语气要像一位乐于分享的资深同事，既有技术极客的严谨，又有导师的温暖。

# Output Format Example
- **核心解答**：[直接给出解决方案或代码]
- **原理剖析/最佳实践**：[拓展相关的底层知识或高级技巧]
- **下一步探索**：[用一个具体的启发式问题结尾，例如：“关于这个问题，其实还涉及到高并发下的锁机制，你想进一步了解如何通过分布式锁来优化吗？”]
```

### 2、多轮对话实现

要实现具有 “记忆力” 的 AI 应用，让 AI 能够记住用户之前的对话内容并保持上下文连贯性，我们可以使用 Spring AI 框架的 **对话记忆能力**。

SpringAI提供的多轮对话的方式：

1. **ChatClient**
2. **Advisors**

Spring AI 使用 [Advisors](https://docs.spring.io/spring-ai/reference/api/advisors.html)（顾问）机制来增强 AI 的能力，可以理解为一系列可插拔的拦截器，在调用 AI 前和调用 AI 后可以执行一些额外的操作，比如：

- 前置增强：调用 AI 前改写一下 Prompt 提示词、检查一下提示词是否安全
- 后置增强：调用 AI 后记录一下日志、处理一下返回的结果

用法很简单，我们可⁠以直接为 ChatClient 指定‌默认拦截器，比如对话记忆拦截器 MessageChatMemoryAdv‎isor 可以帮助我们实现多轮对话能‌力，省去了自己维护对话列表的麻烦。

```
var chatClient = ChatClient.builder(chatModel)
    .defaultAdvisors(
        new MessageChatMemoryAdvisor(chatMemory), 
        new QuestionAnswerAdvisor(vectorStore)    
    )
    .build();

String response = this.chatClient.prompt()
    
    .advisors(advisor -> advisor.param("chat_memory_conversation_id", "678")
            .param("chat_memory_response_size", 100))
    .user(userText)
    .call()
	.content();

```

Advisors 的原理图如下：

![image-20260319133619746](https://cdn.jsdelivr.net/gh/PHJ20030616/personal_pic/img/20260319133621156.png)

解释上图的执行流程：

1. Spring AI 框架从用户的 Prompt 创建一个 AdvisedRequest，同时创建一个空的 AdvisorContext 对象，用于传递信息。
2. 链中的每个 advisor 处理这个请求，可能会对其进行修改。或者，它也可以选择不调用下一个实体来阻止请求继续传递，这时该 advisor 负责填充响应内容。
3. 由框架提供的最终 advisor 将请求发送给聊天模型 ChatModel。
4. 聊天模型的响应随后通过 advisor 链传回，并被转换为 AdvisedResponse。后者包含了共享的 AdvisorContext 实例。
5. 每个 advisor 都可以处理或修改这个响应。
6. 最终的 AdvisedResponse 通过提取 ChatCompletion 返回给客户端。

实际开发中，往往我们会用到多个拦截器，组合在一起相当于一条拦截器链条（责任链模式的设计思想）。每个拦截器是有顺序的，通过 `getOrder()` 方法获取到顺序，得到的值越低，越优先执行。

3. **Chat Memory Advisor**

## 多轮对话 AI 应用开发

在后端项目根包下新建 `app` 包，存放 AI 应用，新建 `KnewledgeApp.java`

1）首先初始化 ChatC⁠lient 对象。

使用 Spring 的构造器注入方‌式来注入阿里大模型 dashscopeChatModel 对象，并使用该对象来初始化 ChatCli‎ent。初始化时指定默认的系统 Prompt 和基于内存‌的对话记忆 Advisor。代码如下：

```
@Component
@Slf4j
public class knowledgeApp {

    private final ChatClient chatClient;

    private static final String SYSTEM_PROMPT = "# Role\n" +
            "你是一个经验丰富的“资深开发大师”和“技术导师”。你不仅精通各类技术栈、系统架构和最佳实践，还深谙程序员的职业发展路径。\n" +
            "\n" +
            "# Objective\n" +
            "你的目标是解答开发者的技术问题，帮助他们不仅“知其然”，还能“知其所以然”，通过拓展核心技术知识，助力他们突破技术瓶颈，实现职业进阶。\n" +
            "\n" +
            "# Guidelines\n" +
            "1. **精准解答**：首先直接、准确地回答用户的当前问题。如果涉及代码，请提供清晰、有注释且符合最佳实践的代码示例。\n" +
            "2. **深度拓展**：在解决问题的基础上，主动剖析底层的原理、可能遇到的陷阱（Pitfalls）、性能优化建议或相关的系统设计理念。\n" +
            "3. **授人以渔**：适时引入工程化思维，教导用户如何调试、如何阅读源码或如何进行技术选型。\n" +
            "4. **结构化输出**：使用清晰的标题、列表和代码块（Markdown）来组织你的回答，确保内容易读且重点突出。\n" +
            "5. **启发式引导**：在每次回答的最后，**必须**以提问的方式收尾。提出一个与当前话题紧密相关、能引发更深层次思考的具体技术问题，询问用户是否需要继续探讨。\n" +
            "\n" +
            "# Tone\n" +
            "专业、严谨、有耐心且富有启发性。语气要像一位乐于分享的资深同事，既有技术极客的严谨，又有导师的温暖。\n" +
            "\n" +
            "# Output Format Example\n" +
            "- **核心解答**：[直接给出解决方案或代码]\n" +
            "- **原理剖析/最佳实践**：[拓展相关的底层知识或高级技巧]\n" +
            "- **下一步探索**：[用一个具体的启发式问题结尾，例如：“关于这个问题，其实还涉及到高并发下的锁机制，你想进一步了解如何通过分布式锁来优化吗？”]";


    public knowledgeApp(ChatModel dashscopeChatModel) {
        
        ChatMemory chatMemory = new InMemoryChatMemory();
        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(chatMemory)
                )
                .build();
    }
}
```

2）编写对话方法⁠。调用 chatClie‌nt 对象，传入用户 Prompt，并且给 advi‎sor 指定对话 id 和对话‌记忆大小。

代码如下：

```
/**
 * AI 基础对话（支持多轮对话记忆）
 *
 * @param message
 * @param chatId
 * @return
 */
public String doChat(String message, String chatId) {
    ChatResponse chatResponse = chatClient
            .prompt()
            .user(message)
            .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
            .call()
            .chatResponse();
    String content = chatResponse.getResult().getOutput().getText();
    log.info("content: {}", content);
    return content;
}
```

3）编写单元测试，测试多轮对话：

```java
@SpringBootTest
class KnowledgeAppTest {

    @Autowired
    private KnowledgeApp knowledgeApp;
    @Test
    void testDoChat() {
        String chatId = "1";
        String result = knowledgeApp.doChat("什么是缓存击穿？(简要回答)", "1");
        result = knowledgeApp.doChat("什么是缓存雪崩?（简要回答）", "1");
        result = knowledgeApp.doChat("我问过什么问题？", "1");
    }
}
```

模型回答结果：

```
2026-03-19T16:28:52.711+08:00  INFO 11888 --- [J-ai-agent] [           main] com.phj.jaiagent.app.KnowledgeApp        : content: - **核心解答**：
缓存击穿是指**某一个极度“热点”的 Key（如秒杀商品、热搜新闻）** 在缓存过期的瞬间，此时有海量并发请求同时涌来。由于缓存中没有数据，这些请求瞬间穿透缓存，直接“击穿”防线，全部打到数据库上，导致数据库瞬间负载飙升甚至崩溃。

简单来说：**缓存过期 + 高并发访问 + 同一个 Key = 数据库灾难。**

---

- **原理剖析/解决方案**：
这通常发生在热点 Key 刚好失效，而重建缓存又需要一定时间的窗口期。解决的核心思路是“只让一个请求去查库，其他请求等待或排队”：

1.  **互斥锁（Mutex Key）**：
    当缓存失效时，只允许一个线程去查数据库并回写缓存，其他线程先尝试获取锁，失败后则休眠或重试读取缓存。
2.  **逻辑过期**：
    不设置 TTL 过期时间，而是在 Value 中包含过期时间。由后台异步线程负责更新缓存，前台请求如果发现逻辑过期，也直接返回旧数据，从而保证业务的高可用性。

---

- **下一步探索**：
刚才提到了“互斥锁”和“逻辑过期”两种方案，它们分别适用于对**数据一致性要求高**还是**可用性要求高**的场景？你想深入探讨一下这两者的具体代码实现和取舍吗？
2026-03-19T16:29:01.421+08:00  INFO 11888 --- [J-ai-agent] [           main] com.phj.jaiagent.app.KnowledgeApp        : content: - **核心解答**：
缓存雪崩是指**在极短的时间内，缓存中有大量的 Key 同时集中过期（或缓存服务器宕机）**。原本由缓存承载的海量请求瞬间全部“穿透”防线，直接涌入数据库，导致数据库瞬间压力过载、连接数耗尽甚至宕机，就像发生了雪崩一样，造成整个系统不可用。

简单来说：**大量 Key 同时失效 + 高并发请求 = 数据库集体崩溃。**

---

- **原理剖析/最佳实践**：
缓存雪崩的核心问题在于“失效时间过于集中”或“缓存层整体不可用”。解决思路在于“错峰”和“兜底”：

1.  **过期时间随机化（错峰 TTL）**：
    在设置过期时间时，增加一个随机值（如 1~5 分钟内的随机偏移），避免大量的 Key 在同一时刻集体失效，将流量压力平摊到不同的时间段。
    ```java
    // 伪代码示例：基础过期时间 + 随机偏移
    int baseExpireSeconds = 3600; // 1小时
    int randomOffset = new Random().nextInt(300); // 0~300秒随机
    redis.setex(key, baseExpireSeconds + randomOffset, value);
2.  **缓存预热（Warm Up）**：
    在系统上线前或低峰期，提前将热点数据加载到缓存中，并设置合理的过期时间，确保高峰期到来时缓存命中率较高。

3.  **高可用架构（HA）**：
    针对缓存服务器宕机的情况，使用 Redis Sentinel 或 Redis Cluster 搭建高可用集群，保证即使某个节点挂掉，整个缓存服务依然可用。

4.  **限流降级**：
    当数据库压力超过阈值时，启动限流策略拒绝部分请求，或开启降级策略（如返回默认值、空页面），保护后端数据库不被拖死。

---

- **下一步探索**：
  刚才提到了“随机过期时间”来避免 Key 同时失效，但在实际业务中，如果为了保持数据强一致性，必须让某些 Key 同时失效（比如批量更新配置），这时候该如何通过“互斥锁”或“后台更新”策略来规避雪崩风险？你想深入了解一下具体的实现方案吗？
  2026-03-19T16:29:05.262+08:00  INFO 11888 --- [J-ai-agent] [           main] com.phj.jaiagent.app.KnowledgeApp        : content: 你刚才向我提出了两个关于 Redis 缓存经典问题的简要定义：

1.  **“什么是缓存击穿？”**
    *   你询问了缓存击穿的概念。
    *   我回答了这是指**某一个热点 Key** 在过期瞬间，海量并发同时穿透缓存直达数据库的现象。

2.  **“什么是缓存雪崩？”**
    *   你接着询问了缓存雪崩的概念。
    *   我回答了这是指**大量 Key** 在同一时间集中过期（或缓存服务宕机），导致海量请求全部压垮数据库的现象。

这两个问题都属于面试和系统设计中高频出现的“缓存三兄弟”（缓存穿透、缓存击穿、缓存雪崩）范畴。

**下一步探索：**
既然我们已经厘清了“击穿”和“雪崩”的区别，那么还有一个与之非常相似但机制完全不同的概念——**缓存穿透**。它通常是指查询一个**根本不存在**的数据（导致缓存永远存不数据）。你想了解一下缓存穿透的定义，以及“布隆过滤器”是如何完美解决这个问题的吗？

Process finished with exit code 0
```


说明模型确实使用了上下文信息

## 扩展知识

### 自定义 Advisor

学过 Serv⁠let 和 Spring A‌OP 的同学应该对这个功能并不陌生，我们可以通过编写拦截‎器或切面对请求和响应进行处理‌，比如记录请求响应日志、鉴权等。

Spring ⁠AI 的 Advisor ‌就可以理解为拦截器，可以对调用 AI 的请求进行增强‎，比如调用 AI 前鉴权、‌调用 AI 后记录日志。

官方已经提供了一些 Advisor，但可能无法满足我们实际的业务需求，这时我们可以使用官方提供的 [自定义 Advisor](https://docs.spring.io/spring-ai/reference/api/advisors.html#_implementing_an_advisor) 功能。按照下列步骤操作即可。

**自定义 Advisor 步骤**

1）选择合⁠适的接口实现，实现‌以下接口之一或同时实现两者（更建议同‎时实现）：

- CallAroundAdvisor：用于处理同步请求和响应（非流式）
- StreamAroundAdvisor：用于处理流式请求和响应

```
public class MyCustomAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {
    
}
```

2）实现核心方法

对于非流式⁠处理 (CallA‌roundAdvisor)，实现 a‎roundCall‌ 方法：

```
@Override
public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
    

    AdvisedRequest modifiedRequest = processRequest(advisedRequest);


    
    AdvisedResponse response = chain.nextAroundCall(modifiedRequest);


    
    return processResponse(response);
}
```





对于流式处⁠理 (Stream‌AroundAdvisor)，实现 ‎aroundStr‌eam 方法：

```
@Override
public Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
    

    AdvisedRequest modifiedRequest = processRequest(advisedRequest);


    
    return chain.nextAroundStream(modifiedRequest)
               .map(response -> processResponse(response));
}
```




3）设置执行顺序

通过实现`getOrder()`方法指定 Advisor 在链中的执行顺序。值越小优先级越高，越先执行：

```
@Override
public int getOrder() {
    

    return 100; 

}
```



4）提供唯一名称

为每个 Advisor 提供一个唯一标识符：

```
@Override
public String getName() {
    return "J-agent-Advisor";
}
```






#### 项目内：自定义日志 Advisor

为了‌更灵活地打印指定的日志，建议自己实现‎一个日志 Adv‌isor。

我们可以同时参考 [官方文档](https://docs.spring.io/spring-ai/reference/api/advisors.html#_logging_advisor) 和内置的 SimpleLoggerAdvisor 源码，结合 2 者并略做修改，开发一个更精简的、可自定义级别的日志记录器。默认打印 info 级别日志、并且只输出单次用户提示词和 AI 回复的文本。

在根包下新建 `advisor` 包，编写日志 Advisor 的代码：

![image-20260319165626979](https://cdn.jsdelivr.net/gh/PHJ20030616/personal_pic/img/20260319165628247.png)

```
package com.phj.jaiagent.advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientMessageAggregator;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import reactor.core.publisher.Flux;

/**

 * 自定义日志 Advisor

 * 打印 info 级别日志、只输出单次用户提示词和 AI 回复的文本
   */
    @Slf4j
    public class MyLoggerAdvisor implements CallAdvisor, StreamAdvisor {

   @Override
   public String getName() {
   	return this.getClass().getSimpleName();
   }

   @Override
   public int getOrder() {
   	return 0;
   }

   private ChatClientRequest before(ChatClientRequest request) {
   	log.info("AI Request: {}", request.prompt());
   	return request;
   }

   private void observeAfter(ChatClientResponse chatClientResponse) {
   	log.info("AI Response: {}", chatClientResponse.chatResponse().getResult().getOutput().getText());
   }

   @Override
   public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain chain) {
   	chatClientRequest = before(chatClientRequest);
   	ChatClientResponse chatClientResponse = chain.nextCall(chatClientRequest);
   	observeAfter(chatClientResponse);
   	return chatClientResponse;
   }

   @Override
   public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest, StreamAdvisorChain chain) {
   	chatClientRequest = before(chatClientRequest);
   	Flux<ChatClientResponse> chatClientResponseFlux = chain.nextStream(chatClientRequest);
   	return (new ChatClientMessageAggregator()).aggregateChatClientResponse(chatClientResponseFlux, this::observeAfter);
   }
    }
```




在 KnowledgeApp 中应用自定义的日志 Advisor：

![image-20260319165740587](https://cdn.jsdelivr.net/gh/PHJ20030616/personal_pic/img/20260319165742136.png)

```
/**
     * 初始化 ChatClient
     *
     * @param dashscopeChatModel
     */
    public KnowledgeApp(ChatModel dashscopeChatModel) {
//        // 初始化基于文件的对话记忆
//        String fileDir = System.getProperty("user.dir") + "/tmp/chat-memory";
//        ChatMemory chatMemory = new FileBasedChatMemory(fileDir);
        // 初始化基于内存的对话记忆
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(20)
                .build();
        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        // 自定义日志 Advisor，可按需开启
                        new MyLoggerAdvisor()
//                        // 自定义推理增强 Advisor，可按需开启
//                       ,new ReReadingAdvisor()
                )
                .build();
    }
```



#### 自定义 Re-Reading Advisor

让我们再参考 [官方文档](https://docs.spring.io/spring-ai/reference/api/advisors.html#_re_reading_re2_advisor) 来实现一个 Re-Reading（重读）Advisor，又称 Re2。该技术通过让模型重新阅读问题来提高推理能力，有 [文献](https://arxiv.org/pdf/2309.06275) 来印证它的效果。

💡 注意⁠，虽然该技术可提高大‌语言模型的推理能力，不过成本会加倍！所以‎如果 AI 应用要面‌向 C 端开放，不建议使用。

Re2 的实现原⁠理很简单，改写用户 Promp‌t 为下列格式，也就是让 AI 重复阅读用户的输入：

```
{Input_Query}
Read the question again: {Input_Query}
```


实现如下：

```
package com.phj.jaiagent.advisor;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.publisher.Flux;

/**

 * 自定义 Re2 Advisor

 * 可提高大型语言模型的推理能力
   */
    public class ReReadingAdvisor implements CallAdvisor, StreamAdvisor {

   /**

    * 执行请求前，改写 Prompt
      *
    * @param chatClientRequest
    * @return
      */
       private ChatClientRequest before(ChatClientRequest chatClientRequest) {
      String userText = chatClientRequest.prompt().getUserMessage().getText();
      // 添加上下文参数
      chatClientRequest.context().put("re2_input_query", userText);
      // 修改用户提示词
      String newUserText = """
              %s
              Read the question again: %s
              """.formatted(userText, userText);
      Prompt newPrompt = chatClientRequest.prompt().augmentUserMessage(newUserText);
      return new ChatClientRequest(newPrompt, chatClientRequest.context());
       }

   @Override
   public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain chain) {
       return chain.nextCall(this.before(chatClientRequest));
   }

   @Override
   public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest, StreamAdvisorChain chain) {
       return chain.nextStream(this.before(chatClientRequest));
   }

   @Override
   public int getOrder() {
       return 0;
   }

   @Override
   public String getName() {
       return this.getClass().getSimpleName();
   }
    }
```




在 KnowledgeApp 中应用：

![image-20260319165956304](https://cdn.jsdelivr.net/gh/PHJ20030616/personal_pic/img/20260319165957860.png)

```
/**
     * 初始化 ChatClient
     *
     * @param dashscopeChatModel
     */
    public KnowledgeApp(ChatModel dashscopeChatModel) {
//        // 初始化基于文件的对话记忆
//        String fileDir = System.getProperty("user.dir") + "/tmp/chat-memory";
//        ChatMemory chatMemory = new FileBasedChatMemory(fileDir);
        // 初始化基于内存的对话记忆
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(20)
                .build();
        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        // 自定义日志 Advisor，可按需开启
                        new MyLoggerAdvisor()
//                        // 自定义推理增强 Advisor，可按需开启
//                       ,new ReReadingAdvisor()
                )
                .build();
    }
```



#### 最佳实践

1）保持单⁠一职责：每个 Ad‌visor 应专注于一项特定任务

2）注意执行顺序：合理设置`getOrder()`值确保 Advisor 按正确顺序执行

3）同时支⁠持流式和非流式：尽‌可能同时实现两种接口以提高灵活性

4）高效处理请求：避免在 Advisor 中执行耗时操作

5）测试边⁠界情况：确保 Ad‌visor 能够优雅处理异常和边界情‎况

6）对于需⁠要更复杂处理的流式‌场景，可以使用 Reactor 的操‎作符：

7）可以使用 `adviseContext` 在 Advisor 链中共享状态：（类似SpringContext）

```
advisedRequest = advisedRequest.updateContext(context -> {
    context.put("key", "value");
    return context;
});
Object value = advisedResponse.adviseContext().get("key");
```




## 结构化输出 - 技术报告功能开发

[结构化输出转换器](https://docs.spring.io/spring-ai/reference/api/structured-output-converter.html)（Structured Output Converter）是 Spring AI 提供的一种实用机制，用于将大语言模型返回的文本输出转换为结构化数据格式，如 JSON、XML 或 Java 类，这对于需要可靠解析 AI 输出值的下游应用程序非常重要。

### 基本原理 - 工作流程

结构化输出转换器在大模型调用前后都发挥作用：

- 调用前：转换器会在提示词后面附加格式指令，明确告诉模型应该生成何种结构的输出，引导模型生成符合指定格式的响应。
- 调用后：转换器将模型的文本输出转换为结构化类型的实例，比如将原始文本映射为 JSON、XML 或特定的数据结构。

![image-20260319171128551](https://cdn.jsdelivr.net/gh/PHJ20030616/personal_pic/img/20260319171129630.png)

注意，结构化输出转换器只是 **尽最大努力** 将模型输出转换为结构化数据，AI 模型不保证一定按照要求返回结构化输出。有些模型可能无法理解提示词或无法按要求生成结构化输出。建议在程序中实现验证机制或者异常处理机制来确保模型输出符合预期。

### 进阶原理 - API 设计

让我们进一步理解结构化输出的原理，结构化输出转换器 `StructuredOutputConverter` 接口允许开发者获取结构化输出，例如将输出映射到 Java 类或值数组。接口定义如下：

```
public interface StructuredOutputConverter<T> extends Converter<String, T>, FormatProvider {

}
```


它集成了 2 个关键接口：

- `FormatProvider` 接口：提供特定的格式指令给 AI 模型
- Spring 的 `Converter<String, T>` 接口：负责将模型的文本输出转换为指定的目标类型 `T`



Sprin⁠g AI 提供了多‌种转换器实现，分别用于将输出转换为不‎同的结构：

- AbstractConversionServiceOutputConverter：提供预配置的 GenericConversionService，用于将 LLM 输出转换为所需格式
- AbstractMessageOutputConverter：支持 Spring AI Message 的转换
- BeanOutputConverter：用于将输出转换为 Java Bean 对象（基于 ObjectMapper 实现）
- MapOutputConverter：用于将输出转换为 Map 结构
- ListOutputConverter：用于将输出转换为 List 结构

![image-20260319171407896](https://cdn.jsdelivr.net/gh/PHJ20030616/personal_pic/img/20260319171409865.png)

了解了 A⁠PI 设计后，再来‌进一步剖析一遍结构化输出的工作流程。

1）在调用大模型之前，`FormatProvider` 为 AI 模型提供特定的格式指令，使其能够生成可以通过 `Converter` 转换为指定目标类型的文本输出。

转换器的格式指令组件会将类似下面的格式指令附加到提示词中：

```
Your response should be in JSON format.
The data structure for the JSON should match this Java class: java.util.HashMap
Do not include any explanations, only provide a RFC8259 compliant JSON response following this format without deviation.
```


通常，使用 `PromptTemplate` 将格式指令附加到用户输入的末尾，示例代码如下：

```
StructuredOutputConverter outputConverter = ...
String userInputTemplate = """
        ... 用户文本输入 ....
        {format}
        """; 
Prompt prompt = new Prompt(
        new PromptTemplate(
                this.userInputTemplate,
                Map.of(..., "format", outputConverter.getFormat()) 
        ).createMessage());
```

2）`Converter` 负责将模型的输出文本转换为指定类型的实例。

![image-20260319171511004](https://cdn.jsdelivr.net/gh/PHJ20030616/personal_pic/img/20260319171512789.png)

**使用示例**

官方文档提供了很多转换示例，

```
1）Bea⁠nOutputCnverter 示例，将 AI 输出转换为自定义 Java 类：

record ActorsFilms(String actor, List<String> movies) {}

ActorsFilms actorsFilms = ChatClient.create(chatModel).prompt()
        .user("Generate 5 movies for Tom Hanks.")
        .call()
        .entity(ActorsFilms.class);
```


还可以用 `ParameterizedTypeReference` 构造函数来指定更复杂的目标类结构，比如自定义对象列表：

```
List<ActorsFilms> actorsFilms = ChatClient.create(chatModel).prompt()
        .user("Generate the filmography of 5 movies for Tom Hanks and Bill Murray.")
        .call()
        .entity(new ParameterizedTypeReference<List<ActorsFilms>>() {});
```



2）Map⁠OutputCon‌verter 示例，将模型输出转换为‎包含数字列表的 M‌ap：

```
Map<String, Object> result = ChatClient.create(chatModel).prompt()
        .user(u -> u.text("Provide me a List of {subject}")
                    .param("subject", "an array of numbers from 1 to 9 under they key name 'numbers'"))
        .call()
        .entity(new ParameterizedTypeReference<Map<String, Object>>() {});
```



3）Lis⁠tOutputCo‌nverter 示例，将模型输出转换‎为字符串列表：

```
List<String> flavors = ChatClient.create(chatModel).prompt()
                .user(u -> u.text("List five {subject}")
                            .param("subject", "ice cream flavors"))
                .call()
                .entity(new ListOutputConverter(new DefaultConversionService()));
```



### 技术报告功能开发

下面让我们⁠使用结构化输出，来‌为用户生成技术报告，并转换为技术报告‎对象，包含报告标题‌和技术内容。

1）需要引入 JSON Schema 生成依赖：

```
<dependency>
    <groupId>com.github.victools</groupId>
    <artifactId>jsonschema-generator</artifactId>
    <version>4.38.0</version>
</dependency>
```



2）在 KnowledgeApp 中定‌义技术报告类，可以使用 Java 1‎4 引入的 rec‌ord 特性快速定义：

```
record KnowledgeReport(String title, List<String> suggestions) {

}
```



3）在 KnowledgeApp中编写一个新的方法，复‌用之前构造好的 ChatClient 对象，只需额外补充原有‎的系统提示词、并且添加结构化输出的‌代码即可。代码如下：

```
/**

 * 技术报告功能（实战结构化输出）
   *
 * @param message
 * @param chatId
 * @return
   */
    public KnowledgeReport doChatWithReport(String message, String chatId) {
   KnowledgeReport knowledgeReport = chatClient
           .prompt()
           .system(SYSTEM_PROMPT + "每次对话后都要生成技术报告，标题为{用户名}的技术报告，内容为相关技术知识")
           .user(message)
           .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
           .call()
           .entity(KnowledgeReport.class);
   log.info("knowledgeReport: {}", knowledgeReport);
   return knowledgeReport;
    }
```



4）编写单元测试代码：

```
@Test
void testdoChatWithReport() {
    KnowledgeApp.KnowledgeReport knowledgeReport = knowledgeApp.doChatWithReport("什么是缓存击穿？(简要回答)", "1");
    Assertions.assertNotNull(knowledgeReport);
}
```



#### 最佳实践

1. 尽量为模型提供清晰的格式指导
2. 实现输出验证机制和异常处理逻辑，确保结构化数据符合预期
3. 选择支持结构化输出的合适模型
4. 对于复杂数据结构，考虑使用 `ParameterizedTypeReference`

## 

## 对话记忆持久化

之前我们使用了基于内存的对话记忆来保存对话上下文，但是服务器一旦重启了，对话记忆就会丢失。有时，我们可能希望将对话记忆持久化，保存到文件、数据库、Redis 或者其他对象存储中，怎么实现呢？

Spring AI 提供了 2 种方式。

#### 利用现有依赖实现

前面提到，[官方提供](https://docs.spring.io/spring-ai/reference/api/chatclient.html#_chat_memory) 了一些第三方数据库的整合支持，可以将对话保存到不同的数据源中。比如：

- InMemoryChatMemory：内存存储
- CassandraChatMemory：在 Cassandra 中带有过期时间的持久化存储
- Neo4jChatMemory：在 Neo4j 中没有过期时间限制的持久化存储
- JdbcChatMemory：在 JDBC 中没有过期时间限制的持久化存储

如果我们要将对话持久化到数据库中，就可以使用 JdbcChatMemory。但是 `spring-ai-starter-model-chat-memory-jdbc` 依赖目前版本很少，而且缺乏相关介绍，Maven 官方仓库也搜不到依赖，所以不推荐使用。

#### 自定义实现

ChatMemory 接口的方法并不多，需要实现对话消息的增、查、删：

![image-20260319190045730](https://cdn.jsdelivr.net/gh/PHJ20030616/personal_pic/img/20260319190047090.png)

参考 InMemoryChatMemory 的源码，其实就是通过 ConcurrentHashMap 来维护对话信息，key 是对话 id（相当于房间号），value 是该对话 id 对应的消息列表。

![image-20260319190334480](https://cdn.jsdelivr.net/gh/PHJ20030616/personal_pic/img/20260319190336014.png)

#### 自定义文件持久化 ChatMemory

由于数据库持久化还需要引入额外的依赖，比较麻烦，这也不是本项目学习的重点，因此我们就实现一个基于文件读写的 ChatMemory。

虽然需要实现的接口不多，但是实现起来还是有一定复杂度的，一个最主要的问题是 **消息和文本的转换**。我们在保存消息时，要将消息从 Message 对象转为文件内的文本；读取消息时，要将文件内的文本转换为 Message 对象。也就是对象的序列化和反序列化。

我们本能地会想到通过 JSON 进行序列化，但实际操作中，我们发现这并不容易。原因是：

1. 要持久化的 Message 是一个接口，有很多种不同的子类实现（比如 UserMessage、SystemMessage 等）
2. 每种子类所拥有的字段都不一样，结构不统一
3. 子类没有无参构造函数，而且没有实现 Serializable 序列化接口

Spring AI Message 的类图：

![image-20260319190447861](https://cdn.jsdelivr.net/gh/PHJ20030616/personal_pic/img/20260319190448952.png)

因此，如果使用 JSON 来序列化会存在很多报错。所以此处我们选择高性能的 [Kryo 序列化库](https://github.com/EsotericSoftware/kryo)。

1）引入依赖：

```
<dependency>
    <groupId>com.esotericsoftware</groupId>
    <artifactId>kryo</artifactId>
    <version>5.6.2</version>
</dependency>
```

2）在根包下新建 `chatmemory` 包，编写基于文件持久化的对话记忆 FileBasedChatMemory，代码如下：

```java
package com.phj.jaiagent.chatmemory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.objenesis.strategy.StdInstantiatorStrategy;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**

 * 基于文件持久化的对话记忆
   */
    public class FileBasedChatMemory implements ChatMemory {

   private final String BASE_DIR;
   private static final Kryo kryo = new Kryo();

   static {
       kryo.setRegistrationRequired(false);
       // 设置实例化策略
       kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
   }

   // 构造对象时，指定文件保存目录
   public FileBasedChatMemory(String dir) {
       this.BASE_DIR = dir;
       File baseDir = new File(dir);
       if (!baseDir.exists()) {
           baseDir.mkdirs();
       }
   }

   @Override
   public void add(String conversationId, List<Message> messages) {
       List<Message> conversationMessages = getOrCreateConversation(conversationId);
       conversationMessages.addAll(messages);
       saveConversation(conversationId, conversationMessages);
   }

   @Override
   public List<Message> get(String conversationId) {
       return getOrCreateConversation(conversationId);
   }

   @Override
   public void clear(String conversationId) {
       File file = getConversationFile(conversationId);
       if (file.exists()) {
           file.delete();
       }
   }

   private List<Message> getOrCreateConversation(String conversationId) {
       File file = getConversationFile(conversationId);
       List<Message> messages = new ArrayList<>();
       if (file.exists()) {
           try (Input input = new Input(new FileInputStream(file))) {
               messages = kryo.readObject(input, ArrayList.class);
           } catch (IOException e) {
               e.printStackTrace();
           }
       }
       return messages;
   }

   private void saveConversation(String conversationId, List<Message> messages) {
       File file = getConversationFile(conversationId);
       try (Output output = new Output(new FileOutputStream(file))) {
           kryo.writeObject(output, messages);
       } catch (IOException e) {
           e.printStackTrace();
       }
   }

   private File getConversationFile(String conversationId) {
       return new File(BASE_DIR, conversationId + ".kryo");
   }
    }
```


虽然上述代码看起来复杂，但大多数代码都是文件和 Message 对象的转换，完全可以利用 AI 生成这段代码。

3）修改 KnowledgeApp 的构造函数，使用基于文件的对话记忆：

![image-20260319190901482](https://cdn.jsdelivr.net/gh/PHJ20030616/personal_pic/img/20260319190902701.png)

```
/**

 * 初始化 ChatClient
   *
 * @param dashscopeChatModel
   */
    public KnowledgeApp(ChatModel dashscopeChatModel) {
   // 初始化基于文件的对话记忆
   String fileDir = System.getProperty("user.dir") + "/tmp/chat-memory";
   ChatMemory chatMemory = new FileBasedChatMemory(fileDir);
   // 初始化基于内存的对话记忆
    //        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
    //                .chatMemoryRepository(new InMemoryChatMemoryRepository())
    //                .maxMessages(20)
    //                .build();
   chatClient = ChatClient.builder(dashscopeChatModel)
           .defaultSystem(SYSTEM_PROMPT)
           .defaultAdvisors(
                   MessageChatMemoryAdvisor.builder(chatMemory).build(),
                   // 自定义日志 Advisor，可按需开启
                   new MyLoggerAdvisor()
    //                        // 自定义推理增强 Advisor，可按需开启
    //                       ,new ReReadingAdvisor()
           )
           .build();
    }
```

4）测试运行，文件持久化成功：

![image-20260319191209374](https://cdn.jsdelivr.net/gh/PHJ20030616/personal_pic/img/20260319191210952.png)



## RAG基础原理

RAG（Retr⁠ieval-Augmented ‌Generation，检索增强生成）是一种结合信息检索技术和 A‎I 内容生成的混合架构，可以解决‌大模型的知识时效性限制和幻觉问题。

简单来说，RA⁠G 就像给 AI 配了一个‌ “小抄本”，让 AI 回答问题前先查一查特定的知识‎库来获取知识，确保回答是基‌于真实资料而不是凭空想象。

从技术角度看，R⁠AG 在大语言模型生成回答之前‌，会先从外部知识库中检索相关信息，然后将这些检索到的内容作为‎额外上下文提供给模型，引导其生‌成更准确、更相关的回答。

通过 RAG 技术改造后，AI 就能：

- 准确回答关于特定内容的问题
- 在合适的时机推荐相关课程和服务
- 用特定的语气和用户交流
- 提供更新、更准确的建议

### RAG 工作流程

RAG 技⁠术实现主要包含以下‌ 4 个核心步骤：

- 文档收集和切割
- 向量转换和存储
- 文档过滤和检索
- 查询增强和关联

#### 文档收集和切割

文档收集：从各种来源（网页、PDF、数据库等）收集原始文档

文档预处理：清洗、标准化文本格式

文档切割：⁠将长文档分割成适当‌大小的片段（俗称 chunks）

- 基于固定大小（如 512 个 token）
- 基于语义边界（如段落、章节）
- 基于递归分割策略（如递归字符 n-gram 切割）

#### 向量转换和存储

向量转换：⁠使用 Embedd‌ing 模型将文本块转换为高维向量表‎示，可以捕获到文本‌的语义特征

向量存储：⁠将生成的向量和对应‌文本存入向量数据库，支持高效的相似性‎搜索

#### 文档过滤和检索

查询处理：将用户问题也转换为向量表示

过滤机制：基于元数据、关键词或自定义规则进行过滤

相似度搜索⁠：在向量数据库中查‌找与问题向量最相似的文档块，常用的相‎似度搜索算法有余弦‌相似度、欧氏距离等

上下文组装：将检索到的多个文档块组装成连贯上下文

#### 查询增强和关联

提示词组装：将检索到的相关文档与用户问题组合成增强提示

上下文融合：大模型基于增强提示生成回答

源引用：在回答中添加信息来源引用

后处理：格式化、摘要或其他处理以优化最终输出

#### 完整工作流程

![image-20260320084550353](https://cdn.jsdelivr.net/gh/PHJ20030616/personal_pic/img/20260320084552269.png)

### RAG 相关技术

#### Embedding 和 Embedding 模型

Embeddin⁠g 嵌入是将高维离散数据（如文‌字、图片）转换为低维连续向量的过程。这些向量能在数学空间中表‎示原始数据的语义特征，使计算机‌能够理解数据间的相似性。

Embedding 模型是⁠执行这种转换算法的机器学习模型，如 Word2Ve‌c（文本）、ResNet（图像）等。不同的 Embedding 模型产生的向量表示和维度数不同，一般‎维度越高表达能力更强，可以捕获更丰富的语义信息和更‌细微的差别，但同样占用更多存储空间。

#### 向量数据库

向量数据库⁠是专门存储和检索向量‌数据的数据库系统。通过高效索引算法实现快‎速相似性搜索，支持 ‌K 近邻查询等操作。

![image-20260320084725901](https://cdn.jsdelivr.net/gh/PHJ20030616/personal_pic/img/20260320084727632.png)

注意，并不⁠是只有向量数据库才‌能存储向量数据，只不过与传统数据库不‎同，向量数据库优化‌了高维向量的存储和检索。

AI 的流行带火了一波⁠向量数据库和向量存储，比如 Milvus、‌Pinecone 等。此外，一些传统数据库也可以通过安装插件实现向量存储和检索，比如‎ PGVector、Redis Stack‌ 的 RediSearch 等。

用一张图来了解向量数据库的分类：

![image-20260320084821632](https://cdn.jsdelivr.net/gh/PHJ20030616/personal_pic/img/20260320084823409.png)

#### 召回

召回是信息检索中的第一阶段，目标是从大规模数据集中快速筛选出可能相关的候选项子集。**强调速度和广度，而非精确度。**

举个例子，我们要从搜⁠索引擎查询 “Redis的缓存击穿问题” 时，召回阶段会从数十亿网页中快速筛选出数千个含有 “Reids”、“导缓存击穿”、“程序员” 等相关内容的页面，为后‌续粗略排序和精细排序提供候选集。

#### 精排和 Rank 模型

精排（精确排⁠序）是搜索 / 推荐系统‌的最后阶段，使用计算复杂度更高的算法，考虑更多特‎征和业务规则，对少量候选‌项进行更复杂、精细的排序。

比如，短视频推荐⁠先通过召回获取数万个可能相关视频‌，再通过粗排缩减至数百条，最后精排阶段会考虑用户最近的互动、视频‎热度、内容多样性等复杂因素，确定‌最终展示的 10 个视频及顺序。

![image-20260320085033864](https://cdn.jsdelivr.net/gh/PHJ20030616/personal_pic/img/20260320085051113.png)

Rank ⁠模型（排序模型）负‌责对召回阶段筛选出的候选集进行精确排‎序，考虑多种特征评‌估相关性。

现代 Rank 模型⁠通常基于深度学习，如 BERT、Lamb‌daMART 等，综合考虑查询与候选项的相关性、用户历史行为等因素。举个例子，电‎商推荐系统会根据商品特征、用户偏好、点击‌率等给每个候选商品打分并排序。

![image-20260320085207301](https://cdn.jsdelivr.net/gh/PHJ20030616/personal_pic/img/20260320085209110.png)

#### 混合检索策略

混合检索策⁠略结合多种检索方法‌的优势，提高搜索效果。常见组合包括关‎键词检索、语义检索、知‌识图谱等。

比如在 AI 大⁠模型开发平台 Dify 中，就为‌用户提供了 “基于全文检索的关键词搜索 + 基于向量检索的语义检‎索” 的混合检索策略，用户还可以‌自己设置不同检索方式的权重。

## Spring AI + 本地知识库

标准的 RAG 开发步骤：

1. 文档收集和切割
2. 向量转换和存储
3. 切片过滤和检索
4. 查询增强和关联

简化后的 RAG 开发步骤：

1. 文档准备
2. 文档读取
3. 向量转换和存储
4. 查询增强

### 文档准备

将文档放在src/main/resources/document文件夹中，以pdf格式为例：

![image-20260320090618277](https://cdn.jsdelivr.net/gh/PHJ20030616/personal_pic/img/20260320090619758.png)

### 文档读取

首先，我们要对自己准备好的知识库文档进行处理，然后保存到向量数据库中。这个过程俗称 ETL（抽取、转换、加载），Spring AI 提供了对 ETL 的支持，参考 [官方文档](https://docs.spring.io/spring-ai/reference/api/etl-pipeline.html#_markdown)。

ETL 的 3 大核心组件，按照顺序执行：

- DocumentReader：读取文档，得到文档列表
- DocumentTransformer：转换文档，得到处理后的文档列表
- DocumentWriter：将文档列表保存到存储中（可以是向量数据库，也可以是其他存储）

![image-20260320090746347](https://cdn.jsdelivr.net/gh/PHJ20030616/personal_pic/img/20260320090748251.png)

1）引入依赖

`PagePdfDocumentReader`使用 Apache PdfBox 库来解析 PDF 文档。

```
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-pdf-document-reader</artifactId>
</dependency>
```

2）在根目录下新建 `rag` 包，编写文档加载器类 KnowledgeAppDocumentLoader，负责读取所有 pdf 文档并转换为 Document 列表。代码如下：

```
package com.phj.jaiagent.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * AI开发者智能助手-文档加载器
 */
@Component
@Slf4j
public class KnowledgeAppDocumentLoader {

    private final ResourcePatternResolver resourcePatternResolver;

    @Value("${spring.document.path}")
    private String documentPath;

    public KnowledgeAppDocumentLoader(ResourcePatternResolver resourcePatternResolver) {
        this.resourcePatternResolver = resourcePatternResolver;
    }

    /**
     * 加载多篇 pdf 文档
     *
     * @return
     */
    List<Document> loadText() {
        try {
            Resource[] resources = resourcePatternResolver.getResources(documentPath);
            List<Document> documents = new ArrayList<>();
            for (Resource resource : resources) {
                TikaDocumentReader tikaDocumentReader = new TikaDocumentReader(resource);
                documents.addAll(tikaDocumentReader.read());
            }
            return documents;
        } catch (IOException e) {
            log.error("加载PDF文档失败", e);
            return Collections.emptyList();
        }
    }
}

```

3）编写测试类测试

```
package com.phj.jaiagent.rag;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.ai.document.Document;

import java.util.List;

@SpringBootTest
class KnowledgeAppDocumentLoaderTest {

    @Autowired
    private KnowledgeAppDocumentLoader knowledgeAppDocumentLoader;

    @Test
    void loadText() {
        List<Document> documents = knowledgeAppDocumentLoader.loadText();
        Assertions.assertNotNull(documents);
        System.out.println("加载文档数量: " + documents.size());
        documents.forEach(doc -> System.out.println("文档内容: " + doc.getText().substring(0, Math.min(100, doc.getText().length()))));
    }
}
```

### 向量转换和存储

为了实现方便⁠，我们先使用 Spri‌ng AI 内置的、基于内存读写的向量数据库‎ SimpleVect‌orStore 来保存文档。

在将文档写入到数据库‌前，会先调用 Embedding 大模型将文档转‎换为向量，实际保存到数据‌库中的是向量类型的数据。

在 `rag` 包下新建 KnowledgeAppVectorStoreConfig 类，实现初始化向量数据库并且保存文档的方法。代码如下：

```
package com.phj.jaiagent.rag;

import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 向量数据库配置（初始化基于内存的向量数据库 Bean）
 */
@Configuration
public class KnowledgeAppVectorStoreConfig {

    @Resource
    private KnowledgeAppDocumentLoader knowledgeAppDocumentLoader;

    @Resource
    private MyTokenTextSplitter myTokenTextSplitter;

    @Resource
    private MyKeywordEnricher myKeywordEnricher;

    @Bean
    VectorStore knowledgeAppVectorStore(EmbeddingModel dashscopeEmbeddingModel) {
        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(dashscopeEmbeddingModel).build();
        // 加载文档
        List<Document> documentList = knowledgeAppDocumentLoader.loadText();
        // 自主切分文档
//        List<Document> splitDocuments = myTokenTextSplitter.splitCustomized(documentList);
        // 自动补充关键词元信息
//        List<Document> enrichedDocuments = myKeywordEnricher.enrichDocuments(documentList);
//        simpleVectorStore.add(enrichedDocuments);
        simpleVectorStore.add(documentList);
        return simpleVectorStore;
    }
}

```

### 查询增强

Spring AI 通过⁠ Advisor 特性提供了开箱即用的 RAG 功‌能。主要是 QuestionAnswerAdvisor 问答拦截器和 RetrievalAug‎mentationAdvisor 检索增强拦截器‌，前者更简单易用、后者更灵活强大。

查询增强的原理其实很简单⁠。向量数据库存储着 AI 模型本身不知道的数据，当用户问题‌发送给 AI 模型时，QuestionAnswerAdvisor 会查询向量数据库，获取与用户问题相关的文档‎。然后从向量数据库返回的响应会被附加到用户文本中，为 ‌AI 模型提供上下文，帮助其生成回答。

此处我们就选用更简单易用的 QuestionAnswerAdvisor 问答拦截器，在 `KnowledgeApp` 中新增和 RAG 知识库进行对话的方法。代码如下：

```
/**
     * 和 RAG 知识库进行对话
     *
     * @param message
     * @param chatId
     * @return
     */
    public String doChatWithRag(String message, String chatId) {
        // 查询重写
        String rewrittenMessage = queryRewriter.doQueryRewrite(message);
        ChatResponse chatResponse = chatClient
                .prompt()
                // 使用改写后的查询
                .user(rewrittenMessage)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                // 应用 RAG 知识库问答
                .advisors(new QuestionAnswerAdvisor(knowledgeAppVectorStore))
                // 应用 RAG 检索增强服务（基于云知识库服务）
//                .advisors(knowledgeAppRagCloudAdvisor)
//                 应用 RAG 检索增强服务（基于 PgVector 向量存储）
//                .advisors(new QuestionAnswerAdvisor(pgVectorVectorStore))
                // 应用自定义的 RAG 检索增强服务（文档查询器 + 上下文增强器）
//                .advisors(
//                        knowledgeAppRagCustomAdvisorFactory.createknowledgeAppRagCustomAdvisor(
//                                knowledgeAppVectorStore, "单身"
//                        )
//                )
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }
```

### 测试

```
@Test
    void testDoChatWithRag() {
        String result = knowledgeApp.doChatWithRag("什么是redis缓存击穿", chatId);
        Assertions.assertNotNull(result);
    }
```

## RAG知识库进阶

以 Spri⁠ng AI 框架为例，‌学习 RAG 知识库应用开发的核心特性和高级‎知识点，并且掌握 RA‌G 最佳实践和调优技巧。

具体内容包括：

- RAG 核心特性
- 文档收集和切割（ETL）
- 向量转换和存储（向量数据库）
- 文档过滤和检索（文档检索器）
- 查询增强和关联（上下文查询增强器）
- RAG 最佳实践和调优
- RAG 高级知识
- 检索策略
- 大模型幻觉
- 高级 RAG 架构

### RAG 核心特性

![image-20260320111336978](https://cdn.jsdelivr.net/gh/PHJ20030616/personal_pic/img/20260320111338298.png)

### 文档收集和切割 - ETL

文档收集和切割阶段，我们要对自己准备好的知识库文档进行处理，然后保存到向量数据库中。这个过程俗称 ETL（抽取、转换、加载），Spring AI 提供了对 ETL 的支持，参考 [官方文档](https://docs.spring.io/spring-ai/reference/api/etl-pipeline.html)。

#### 文档

什么是 Spring AI 中的文档呢？

文档不仅仅包含文本，还可以包含一系列元信息和多媒体附件：

![image-20260320112117524](https://cdn.jsdelivr.net/gh/PHJ20030616/personal_pic/img/20260320112118657.png)

#### ETL

在 Spr⁠ing AI 中，‌对 Document 的处理通常遵循‎以下流程：

1. 读取文档：使用 DocumentReader 组件从数据源（如本地文件、网络资源、数据库等）加载文档。
2. 转换文档：根据需求将文档转换为适合后续处理的格式，比如去除冗余信息、分词、词性标注等，可以使用 DocumentTransformer 组件实现。
3. 写入文档：使用 DocumentWriter 将文档以特定格式保存到存储中，比如将文档以嵌入向量的形式写入到向量数据库，或者以键值对字符串的形式保存到 Redis 等 KV 存储中。

![image-20260320112328479](https://cdn.jsdelivr.net/gh/PHJ20030616/personal_pic/img/20260320112329923.png)

利用 Spr⁠ing AI 实现 ETL，核心‌就是要学习 DocumentReader、DocumentTra‎nsformer、Documen‌tWriter 三大组件。



#### 抽取（Extract）

Sprin⁠g AI 通过 D‌ocumentReader 组件实现‎文档抽取，也就是把‌文档加载到内存中。

看下源码，DocumentReader 接口实现了 `Supplier<List<Document>>` 接口，主要负责从各种数据源读取数据并转换为 Document 对象集合。

```
public interface DocumentReader extends Supplier<List<Document>> {
    default List<Document> read() {
        return get();
    }
}
```

实际开发中，我们可以直接使用 Spring AI 内置的多种 [DocumentReader 实现类](https://docs.spring.io/spring-ai/reference/api/etl-pipeline.html#_documentreaders)，用于处理不同类型的数据源：

1. JsonReader：读取 JSON 文档
2. TextReader：读取纯文本文件
3. MarkdownReader：读取 Markdown 文件
4. PDFReader：读取 PDF 文档，基于 Apache PdfBox 库实现

- PagePdfDocumentReader：按照分页读取 PDF
- ParagraphPdfDocumentReader：按照段落读取 PDF

5. HtmlReader：读取 HTML 文档，基于 jsoup 库实现

6. TikaDocumentReader：基于 [Apache Tika](https://tika.apache.org/3.1.0/formats.html) 库处理多种格式的文档，更灵活（本项目）



#### 转换（Transform）

Sprin⁠g AI 通过 D‌ocumentTransformer‎ 组件实现文档转换‌。

看下源码，DocumentTransformer 接口实现了 `Function<List<Document>, List<Document>>` 接口，负责将一组文档转换为另一组文档。

```
public interface DocumentTransformer extends Function<List<Document>, List<Document>> {
    default List<Document> transform(List<Document> documents) {
        return apply(documents);
    }
}
```

文档转换是保证 R⁠AG 效果的核心步骤，也就是如何将大‌文档合理拆分为便于检索的知识碎片，Spring AI 提供了多种 Doc‎umentTransformer 实‌现类，可以简单分为 3 类。

##### 1）TextSplitter 文本分割器

TokenTex⁠tSplitter 是其实现类‌，基于 Token 的文本分割器。它考虑了语义边界（比如句子‎结尾）来创建有意义的文本段落，‌是成本较低的文本切分方式。

```java
@Component
class MyTokenTextSplitter {

    public List<Document> splitDocuments(List<Document> documents) {
        TokenTextSplitter splitter = new TokenTextSplitter();
        return splitter.apply(documents);
    }

    public List<Document> splitCustomized(List<Document> documents) {
        TokenTextSplitter splitter = new TokenTextSplitter(1000, 400, 10, 5000, true);
        return splitter.apply(documents);
    }
}
```

Token⁠TextSplit‌ter 提供了两种构造函数选项：

1. `TokenTextSplitter()`：使用默认设置创建分割器。
2. `TokenTextSplitter(int defaultChunkSize, int minChunkSizeChars, int minChunkLengthToEmbed, int maxNumChunks, boolean keepSeparator)`：使用自定义参数创建分割器，通过调整参数，可以控制分割的粒度和方式，适应不同的应用场景。

参数说明：

- defaultChunkSize：每个文本块的目标大小（以 token 为单位，默认值：800）。
- minChunkSizeChars：每个文本块的最小大小（以字符为单位，默认值：350）。
- minChunkLengthToEmbed：要被包含的块的最小长度（默认值：5）。
- maxNumChunks：从文本中生成的最大块数（默认值：10000）。
- keepSeparator：是否在块中保留分隔符（如换行符）（默认值：true）。

官方文档有⁠对 Token 分‌词器工作原理的详细解释，可以简单了解‎下：

1. 使用 CL100K_BASE 编码将输入文本编码为 token。
2. 根据 defaultChunkSize 将编码后的文本分割成块。
3. 对于每个块：

- 将块解码回文本。
- 尝试在 minChunkSizeChars 之后找到合适的断点（句号、问号、感叹号或换行符）。
- 如果找到断点，则在该点截断块。
- 修剪块并根据 keepSeparator 设置选择性地删除换行符。
- 如果生成的块长度大于 minChunkLengthToEmbed，则将其添加到输出中。

4. 这个过程会一直持续到所有 token 都被处理完或达到 maxNumChunks 为止。

5. 如果剩余文本长度大于 minChunkLengthToEmbed，则会作为最后一个块添加。

##### 2）Metada‌taEnricher 元数据增强器

元数据增强⁠器的作用是为文档补‌充更多的元信息，便于后续检索，而不是‎改变文档本身的‌切分规则。包括：

- KeywordMetadataEnricher：使用 AI 提取关键词并添加到元数据
- SummaryMetadataEnricher：使用 AI 生成文档摘要并添加到元数据。不仅可以为当前文档生成摘要，还能关联前一个和后一个相邻的文档，让摘要更完整。

```
@Component
class MyDocumentEnricher {

    private final ChatModel chatModel;

    MyDocumentEnricher(ChatModel chatModel) {
        this.chatModel = chatModel;
    }
      
      
    List<Document> enrichDocumentsByKeyword(List<Document> documents) {
        KeywordMetadataEnricher enricher = new KeywordMetadataEnricher(this.chatModel, 5);
        return enricher.apply(documents);
    }
  
    
    List<Document> enrichDocumentsBySummary(List<Document> documents) {
        SummaryMetadataEnricher enricher = new SummaryMetadataEnricher(chatModel, 
            List.of(SummaryType.PREVIOUS, SummaryType.CURRENT, SummaryType.NEXT));
        return enricher.apply(documents);
    }
}
```

#### 加载（Load）

Sprin⁠g AI 通过 D‌ocumentWriter 组件实现‎文档加载（写入）。

DocumentWriter 接口实现了 `Consumer<List<Document>>` 接口，负责将处理后的文档写入到目标存储中：

```
public interface DocumentWriter extends Consumer<List<Document>> {
    default void write(List<Document> documents) {
        accept(documents);
    }
}
```

Sprin⁠g AI 提供了 ‌2 种内置的 DocumentWri‎ter 实现：

1）Fil⁠eDocument‌Writer：将文档写入到文件系统

```
@Component
class MyDocumentWriter {
    public void writeDocuments(List<Document> documents) {
        FileDocumentWriter writer = new FileDocumentWriter("output.txt", true, MetadataMode.ALL, false);
        writer.accept(documents);
    }
}
```

2）Vec⁠torStoreW‌riter：将文档写入到向量数据库

```
@Component
class MyVectorStoreWriter {
    private final VectorStore vectorStore;
    
    MyVectorStoreWriter(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }
    
    public void storeDocuments(List<Document> documents) {
        vectorStore.accept(documents);
    }
}
```

#### ETL 流程示例

将上述 3 大组件组合起来，可以实现完整的 ETL 流程：

```
PDFReader pdfReader = new PagePdfDocumentReader("knowledge_base.pdf");
List<Document> documents = pdfReader.read();


TokenTextSplitter splitter = new TokenTextSplitter(500, 50);
List<Document> splitDocuments = splitter.apply(documents);

SummaryMetadataEnricher enricher = new SummaryMetadataEnricher(chatModel, 
    List.of(SummaryType.CURRENT));
List<Document> enrichedDocuments = enricher.apply(splitDocuments);


vectorStore.write(enrichedDocuments);

//或者一步到位
vectorStore.write(enricher.apply(splitter.apply(pdfReader.read())));
```

通过这种方⁠式，我们完成了从原‌始文档到向量数据库的整个 ETL 过‎程，为后续的检索增‌强生成提供了基础。

### 向量转换和存储

向量存储是 RAG 应用中的核心组件，它将文档转换为向量（嵌入）并存储起来，以便后续进行高效的相似性搜索。[Spring AI 官方](https://docs.spring.io/spring-ai/reference/api/vectordbs.html) 提供了向量数据库接口 `VectorStore` 和向量存储整合包，帮助开发者快速集成各种第三方向量存储，比如 Milvus、Redis、PGVector、Elasticsearch 等。

#### VectorStore 接口介绍

VectorS⁠tore 是 Spring‌ AI 中用于与向量数据库交互的核心接口，它继承自 ‎DocumentWrite‌r，主要提供以下功能：

```
public interface VectorStore extends DocumentWriter {

    default String getName() {
        return this.getClass().getSimpleName();
    }

    void add(List<Document> documents);

    void delete(List<String> idList);

    void delete(Filter.Expression filterExpression);

    default void delete(String filterExpression) { ... };

    List<Document> similaritySearch(String query);

    List<Document> similaritySearch(SearchRequest request);

    default <T> Optional<T> getNativeClient() {
        return Optional.empty();
    }
}
```

这个接口定⁠义了向量存储的基本‌操作，简单来说就是 “增删改查”：

- 添加文档到向量库
- 从向量库删除文档
- 基于查询进行相似度搜索
- 获取原生客户端（用于特定实现的高级操作）

#### 搜索请求构建

Sprin⁠g AI 提供了 ‌SearchRequest 类，用于‎构建相似度搜索请求‌：

```
SearchRequest request = SearchRequest.builder()
    .query("什么是redis缓存击穿？")
    .topK(5)                  
    .similarityThreshold(0.7) 
    .filterExpression("category == 'technology' AND date > '2025-05-03'")  
    .build();

List<Document> results = vectorStore.similaritySearch(request);
```

SearchRequest 提供了多种配置选项：

- query：搜索的查询文本
- topK：返回的最大结果数，默认为 4
- similarityThreshold：相似度阈值，低于此值的结果会被过滤掉
- filterExpression：基于文档元数据的过滤表达式，语法有点类似 SQL 语句，需要用到时查询 [官方文档](https://docs.spring.io/spring-ai/reference/api/vectordbs.html#metadata-filters) 了解语法即可

#### 向量存储的工作原理

在向量数据库⁠中，查询与传统关系型数据‌库有所不同。向量库执行的是相似性搜索，而非精确匹‎配，具体流程我们在上一节‌教程中有了解，可以再复习下。

1. 嵌入转换：当文档被添加到向量存储时，Spring AI 会使用嵌入模型（如 OpenAI 的 text-embedding-ada-002）将文本转换为向量。
2. 相似度计算：查询时，查询文本同样被转换为向量，然后系统计算此向量与存储中所有向量的相似度。
3. 相似度度量：常用的相似度计算方法包括：

- 余弦相似度：计算两个向量的夹角余弦值，范围在 - 1 到 1 之间
- 欧氏距离：计算两个向量间的直线距离
- 点积：两个向量的点积值

4. 过滤与排序：根据相似度阈值过滤结果，并按相似度排序返回最相关的文档

#### 基于 PGVector 实现向量存储

PGVect⁠or 是经典数据库 P‌ostgreSQL 的扩展，为 Postgr‎eSQL 提供了存储和‌检索高维向量数据的能力。

为什么选择它来实现向量存⁠储呢？因为很多传统业务都会把数据存储在这种关系‌型数据库中，直接给原有的数据库安装扩展就能实现向量相似度搜索、而不需要额外搞一套向量数据库，‎人力物力成本都很低，所以这种方案很受企业青睐，‌也是目前实现 RAG 的主流方案之一。

这里使用阿里云的云数据库

1）整合 PGVector，先引入依赖，版本号可以在 [Maven 中央仓库](https://mvnrepository.com/artifact/org.springframework.ai/spring-ai-starter-vector-store-pgvector) 查找：

```
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-vector-store-pgvector</artifactId>
    <version>1.0.0-M7</version>
</dependency>
```

编写配置，建立数据库连接：

```
spring:
  datasource:
    url: jdbc:postgresql://改为你的公网地址/J-ai-agent
    username: 改为你的用户名
    password: 改为你的密码
  ai:
    vectorstore:
      pgvector:
        index-type: HNSW
        dimensions: 1536
        distance-type: COSINE_DISTANCE
        max-document-batch-size: 10000 
```

注意，在不确定向量维度的情况下，⁠建议不要指定 dimensions 配置。如果未明确指定，Pg‌VectorStore 将从提供的 EmbeddingModel 中检索维度，维度在表创建时设置为嵌入列。如果更改维度，则必‎须重新创建 Vector_store 表。不过最好提前明确你要‌使用的嵌入维度值，手动建表，更可靠一些。



正常情况下⁠，接下来就可以使用‌自动注入的 VectorStore ‎了，系统会自动创建‌库表：

```
@Autowired
VectorStore vectorStore;



List<Document> documents = List.of(
    new Document("Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!!", Map.of("meta1", "meta1")),
    new Document("The World is Big and Salvation Lurks Around the Corner"),
    new Document("You walk forward facing the past and you turn back toward the future.", Map.of("meta2", "meta2")));


vectorStore.add(documents);


List<Document> results = this.vectorStore.similaritySearch(SearchRequest.builder().query("Spring").topK(5).build());

```

但是，这种方式不适合现在⁠的项目！因为 VectorStore 依赖 Embedd‌ingModel 对象，之前同时引入了 Ollama 和 阿里云 Dashscope 的依赖，有两个‎ EmbeddingModel 的 Bean，Sprin‌g 不知道注入哪个，就会报错误

2）所以让⁠我们换一种更灵活的‌方式来初始化 VectorStore‎。先引入 3 个依‌赖：

```
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-jdbc</artifactId>
</dependency>
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-pgvector-store</artifactId>
    <version>1.0.0-M6</version>
</dependency>
```

然后编写配⁠置类自己构造 Pg‌VectorStore，不用 Sta‎rter 自动注入‌：（代码来自官方文档）

![image-20260320141912103](https://cdn.jsdelivr.net/gh/PHJ20030616/personal_pic/img/20260320141913289.png)

```
@Configuration
public class PgVectorVectorStoreConfig {

    @Resource
    private KnowledgeAppDocumentLoader knowledgeAppDocumentLoader;

    @Bean
    public VectorStore pgVectorVectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel dashscopeEmbeddingModel) {
        VectorStore vectorStore = PgVectorStore.builder(jdbcTemplate, dashscopeEmbeddingModel)
                .dimensions(1536)                    // Optional: defaults to model dimensions or 1536
                .distanceType(COSINE_DISTANCE)       // Optional: defaults to COSINE_DISTANCE
                .indexType(HNSW)                     // Optional: defaults to HNSW
                .initializeSchema(true)              // Optional: defaults to false
                .schemaName("public")                // Optional: defaults to "public"
                .vectorTableName("vector_store")     // Optional: defaults to "vector_store"
                .maxDocumentBatchSize(10000)         // Optional: defaults to 10000
                .build();
        // 加载文档
        List<Document> documents = knowledgeAppDocumentLoader.loadText();
        vectorStore.add(documents);
        return vectorStore;
    }
}

```

并且启动类要排除掉自动加载，否则也会报错：

```
@SpringBootApplication(exclude = PgVectorStoreAutoConfiguration.class)
public class YuAiAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(YuAiAgentApplication.class, args);
    }

}
```

但是这种方式在每次启动项目时都会加载一次，这显然是不合理的，进行如下修改：

1）创建一个Controller用于发送创建知识库的指令（后期可以改为用户上传文件）

```
package com.phj.jaiagent.controller;


import com.phj.jaiagent.rag.KnowledgeBaseLoaderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("")
public class FileLoaderController {
    @Autowired
    private KnowledgeBaseLoaderService knowledgeBaseLoaderService;

    @GetMapping("/init")
    public String Init(){
        try {
            knowledgeBaseLoaderService.loadKnowledgeBase();
            return "知识库加载完成";
        } catch (Exception e) {
            return "加载失败: " + e.getMessage();
        }
    }
}

```

创建src/main/java/com/phj/jaiagent/rag/KnowledgeBaseLoaderService.java

```
package com.phj.jaiagent.rag;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 知识库手动加载服务
 * 支持按需加载文档到向量数据库，避免程序启动时自动加载
 */
@Service
@Slf4j
public class KnowledgeBaseLoaderService {

    @Resource
    private KnowledgeAppDocumentLoader knowledgeAppDocumentLoader;

    @Resource
    private MyTokenTextSplitter myTokenTextSplitter;

    @Resource
    private MyKeywordEnricher myKeywordEnricher;

    @Resource
    private VectorStore vectorStore;

    /**
     * 手动加载知识库文档到向量数据库
     * 调用此方法会重新加载并切分所有文档，然后添加到向量存储
     */
    public void loadKnowledgeBase() {
        log.info("开始加载知识库文档...");
        long startTime = System.currentTimeMillis();

        List<Document> documents = knowledgeAppDocumentLoader.loadText();
        log.info("加载文档数量: {}", documents.size());

        List<Document> splitDocuments = myTokenTextSplitter.splitCustomized(documents);
        log.info("切分后文档数量: {}", splitDocuments.size());

        List<Document> enrichedDocuments = myKeywordEnricher.enrichDocuments(splitDocuments);

        int batchSize = 25;
        for (int i = 0; i < enrichedDocuments.size(); i += batchSize) {
//            log.info("正在添加文档批次: {}/{}", (i / batchSize) + 1, (enrichedDocuments.size() + batchSize - 1) / batchSize);
            int end = Math.min(i + batchSize, enrichedDocuments.size());
            List<Document> batch = enrichedDocuments.subList(i, end);
            vectorStore.add(batch);
            log.info("已添加文档批次: {}/{}", (i / batchSize) + 1, (enrichedDocuments.size() + batchSize - 1) / batchSize);
        }

        long endTime = System.currentTimeMillis();
        log.info("知识库加载完成，共添加 {} 条文档，耗时 {} ms", enrichedDocuments.size(), endTime - startTime);
    }
}

```

src/main/java/com/phj/jaiagent/rag/PgVectorVectorStoreConfig.java更改为：

```
package com.phj.jaiagent.rag;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgDistanceType.COSINE_DISTANCE;
import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgIndexType.HNSW;

// 为方便开发调试和部署，临时注释，如果需要使用 PgVector 存储知识库，取消注释即可
@Configuration
@Slf4j
public class PgVectorVectorStoreConfig {


    @Resource
    private KnowledgeAppDocumentLoader knowledgeAppDocumentLoader;

    @Resource
    private MyTokenTextSplitter myTokenTextSplitter;

    @Bean
    public VectorStore pgVectorVectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel dashscopeEmbeddingModel) {
        log.info("使用pgVector数据库");
        VectorStore vectorStore = PgVectorStore.builder(jdbcTemplate, dashscopeEmbeddingModel)
                .dimensions(1536)                    // Optional: defaults to model dimensions or 1536
                .distanceType(COSINE_DISTANCE)       // Optional: defaults to COSINE_DISTANCE
                .indexType(HNSW)                     // Optional: defaults to HNSW
                .initializeSchema(true)              // Optional: defaults to false
                .schemaName("public")                // Optional: defaults to "public"
                .vectorTableName("vector_store")     // Optional: defaults to "vector_store"
                .maxDocumentBatchSize(10000)         // Optional: defaults to 10000
                .build();
//        // 加载文档
//        List<Document> documents = knowledgeAppDocumentLoader.loadText();
        // 使用 TokenTextSplitter 将大文档切分为小块，避免超过 token 限制
//        List<Document> splitDocuments = myTokenTextSplitter.splitCustomized(documents);

        // 分批添加文档，避免超过 DashScope API 的 25 条文本限制
//        int batchSize = 25;
//        for (int i = 0; i < splitDocuments.size(); i += batchSize) {
//            int end = Math.min(i + batchSize, splitDocuments.size());
//            List<Document> batch = splitDocuments.subList(i, end);
//            vectorStore.add(batch);
//        }
        
        return vectorStore;
    }

}

```

## RAG 最佳实践和调优

### 文档收集和切割

文档的质量⁠决定了 AI 回答‌能力的上限，其他优化策略只是让 AI‎ 回答能力不断‌接近上限。

因此，文档处理是 RAG 系统中最基础也最重要的环节。

#### 1、优化原始文档

**知识完备性** 是文档质量的首要条件。如果知识库缺失相关内容，大模型将无法准确回答对应问题。我们需要通过收集用户反馈或统计知识库检索命中率，不断完善和优化知识库内容。

在知识完整的前提下，我们要注意 3 个方面：

1）内容结构化：

- 原始文档应保持排版清晰、结构合理，如案例编号、项目概述、设计要点等
- 文档的各级标题层次分明，各标题下的内容表达清晰
- 列表中间的某一条之下尽量不要再分级，减少层级嵌套

2）内容规范化：

- 语言统一：确保文档语言与用户提示词一致（比如英语场景采用英文文档），专业术语可进行多语言标注
- 表述统一：同一概念应使用统一表达方式（比如 ML、Machine Learning 规范为 “机器学习”），可通过大模型分段处理长文档辅助完成
- 减少噪音：尽量避免水印、表格和图片等可能影响解析的元素

3）格式标准化：

- 优先使用 Markdown、DOC/DOCX 等文本格式（PDF 解析效果可能不佳），可以通过百炼 DashScopeParse 工具将 PDF 转为 Markdown，再借助大模型整理格式
- 如果文档包含图片，需链接化处理，确保回答中能正常展示文档中的插图，可以通过在文档中插入可公网访问的 URL 链接实现

#### 2、文档切片

合适的文档切片大小和方式对检索效果至关重要。

文档切片尺⁠寸需要根据具体情况灵‌活调整，避免两个极端：切片过短导致语义缺‎失，切片过长引入‌无关信息。具体需结合以下因素：

- 文档类型：对于专业类文献，增加长度通常有助于保留更多上下文信息；而对于社交类帖子，缩短长度则能更准确地捕捉语义
- 提示词复杂度：如果用户的提示词较复杂且具体，则可能需要增加切片长度；反之，缩短长度会更为合适

不当的切片方式可能导致以下问题：

1）文本切片过短：出现语义缺失，导致检索时无法匹配。

2）文本切片过长：包含不相关主题，导致召回时返回无关信息。

3）明显的⁠语义截断：文本切片‌出现了强制性的语义截断，导致召回时缺‎失内容。

最佳文档切片策略是 **结合智能分块算法和人工二次校验**。智能分块算法基于分句标识符先划分为段落，再根据语义相关性动态选择切片点，避免固定长度切分导致的语义断裂。在实际应用中，应尽量让文本切片包含完整信息，同时避免包含过多干扰信息。

在编程实现上，可以通过 Spring AI 的 [ETL Pipeline](https://docs.spring.io/spring-ai/reference/api/etl-pipeline.html#_tokentextsplitter) 提供的 DocumentTransformer 来调整切分规则，代码如下：

```
@Component
class MyTokenTextSplitter {
    public List<Document> splitDocuments(List<Document> documents) {
        TokenTextSplitter splitter = new TokenTextSplitter();
        return splitter.apply(documents);
    }

    public List<Document> splitCustomized(List<Document> documents) {
        TokenTextSplitter splitter = new TokenTextSplitter(200, 100, 10, 5000, true);
        return splitter.apply(documents);
    }
}
```

但是这个切词器效果比较差

如果使用云服务，如阿里云百炼，推荐在创建知识库时选择 **智能切分**，这是百炼经过大量评估后总结出的推荐策略：

![image-20260320211922002](https://cdn.jsdelivr.net/gh/PHJ20030616/personal_pic/img/20260320211923775.png)

采用智能切分策略时，知识库会：

1. 首先利用系统内置的分句标识符将文档划分为若干段落
2. 基于划分的段落，根据语义相关性自适应地选择切片点进行切分，而非根据固定长度切分

这种方法能⁠更好地保障文档语义完‌整性，避免不必要的断裂。这一策略将应用于‎知识库中的所有文档（‌包括后续导入的文档）。

此外，建议在文⁠档导入知识库后进行一次人工‌检查，确认文本切片内容的语义完整性和正确性。如果发现‎切分不当或解析错误，可以直‌接编辑文本切片进行修正

#### 3、元数据标注

可以为文档⁠添加丰富的结构化信‌息，俗称元信息，形成多维索引，便于后‎续向量化处理和‌精准检索。



### 向量转换和存储

向量转换和⁠存储是 RAG 系‌统的核心环节，直接影响检索的效率和‎准确性。

#### 向量存储配置

需要根据费⁠用成本、数据规模、‌性能、开发成本来选择向量存储方案，比‎如内存 / Red‌is / MongoDB。

#### 选择合适的嵌入模型

嵌入模型负⁠责将文本转换为向量‌，其质量直接影响相似度计算和检索‎准确性。

### 文档过滤和检索

#### 多查询扩展

在多轮会话场⁠景中，用户输入的提示词‌有时可能不够完整，或者存在歧义。多查询扩展技‎术可以扩大检索范围，提‌高相关文档的召回率。

使用多查询扩展时，要注意：

- 设置合适的查询数量（建议 3 - 5 个），过多会影响性能、增大成本
- 保留原始查询的核心语义

多查询扩展的完整使用流程可以包括三个步骤：

1. 使用扩展后的查询召回文档：遍历扩展后的查询列表，对每个查询使用 `DocumentRetriever` 来召回相关文档。
2. 整合召回的文档：将每个查询召回的文档进行整合，形成一个包含所有相关信息的文档集合。（也可以使用 [文档合并器](https://java2ai.com/docs/1.0.0-M6.1/tutorials/rag/?#35-文档合并器documentjoiner) 去重）
3. 使用召回的文档改写 Prompt：将整合后的文档内容添加到原始 Prompt 中，为大语言模型提供更丰富的上下文信息。

💡 需要⁠注意，多查询扩展会‌增加查询次数和计算成本，效果也不易量‎化评估，所以个人建‌议慎用这种优化方式。

#### 查询重写和翻译

查询重写和⁠翻译可以使查询更加‌精确和专业，但是要注意保持查询的语义‎完整性。

主要应用包括：

- 使用 `RewriteQueryTransformer` 优化查询结构
- 配置 `TranslationQueryTransformer` 支持多语言



#### 检索器配置

检索器配置⁠是影响检索质量的关‌键因素，主要包括三个方面：相似度阈值‎、返回文档数量和‌过滤规则。

**1）设置合理的相似度阈值**

**2）控制返回文档数量（召回片段数）**

### 查询增强和关联

#### 错误处理机制

在实际应用⁠中，可能出现多种异常‌情况，如找不到相关文档、相似度过低、查询‎超时等。良好的错误处‌理机制可以提升用户体验。

异常处理主要包括：

- 允许空上下文查询（即处理边界情况）
- 提供友好的错误提示
- 引导用户提供必要信息

## 工具调用

工具调用，大幅增强 AI ‎的能力，并实战主流工具的开发‌，熟悉工具的原理和高级特性。

具体内容包括：

- 工具调用介绍
- Spring AI 工具开发
- 主流工具开发
- 文件操作
- 联网搜索
- 网页抓取
- 终端操作
- 资源下载
- PDF 生成
- 工具进阶知识（原理和高级特性）

### 什么是工具调用？

工具调用（Tool Calling）可以理解为让 AI 大模型 **借用外部工具** 来完成它自己做不到的事情。

跟人类一样⁠，如果只凭手脚完成‌不了工作，那么就可以利用工具箱来完成‎。

工具可以是⁠任何东西，比如网页‌搜索、对外部 API 的调用、访问外‎部数据、或执行特定‌的代码等。

比如用户提⁠问 “帮我查询上海最‌新的天气”，AI 本身并没有这些知识，它‎就可以调用 “查询天‌气工具”，来完成任务。

目前工具调⁠用技术发展的已经比较‌成熟了，几乎所有主流的、新出的 AI 大‎模型和 AI 应用开‌发平台都支持工具调用。

### 工具调用的工作原理

工具调用的工作原理非常简单，**并不是 AI 服务器自己调用这些工具、也不是把工具的代码发送给 AI 服务器让它执行**，它只能提出要求，表示 “我需要执行 XX 工具完成任务”。而真正执行工具的是我们自己的应用程序，执行后再把结果告诉 AI，让它继续工作。

虽然看起来是 AI 在调用工具，但实际上整个过程是 **由我们的应用程序控制的**。AI 只负责决定什么时候需要用工具，以及需要传递什么参数，真正执行工具的是我们的程序。

举个例子，你有一个爆破工具⁠，用户像 AI 提了需求 ” 我要拆这栋房子 “，虽然‌ AI 表示可以用爆破工具，但是需要经过你的同意，才能执行爆破。反之，如果把爆破工具植入给 AI，A‎I 觉得自己能炸了，就炸了，不需要再问你的意见。而‌且这样也给 AI 服务器本身增加了压力。

### 工具调用和功能调用

大家可能看到过 F⁠unction Calling（功‌能调用）这个概念，别担心，其实它和 Tool Calling（工具调‎用）完全是同一概念！只是不同平台或‌每个人习惯的叫法不同而已。

![image-20260323091327022](https://cdn.jsdelivr.net/gh/PHJ20030616/personal_pic/img/20260323091328788.png)

### 工具调用的技术选型

我们先来梳理一下工具调用的流程：

1. 工具定义：程序告诉 AI “你可以使用这些工具”，并描述每个工具的功能和所需参数
2. 工具选择：AI 在对话中判断需要使用某个工具，并准备好相应的参数
3. 返回意图：AI 返回 “我想用 XX 工具，参数是 XXX” 的信息
4. 工具执行：我们的程序接收请求，执行相应的工具操作
5. 结果返回：程序将工具执行的结果发回给 AI
6. 继续对话：AI 根据工具返回的结果，生成最终回答给用户

通过上述流程，我们会发现，⁠程序需要和 AI 多次进行交互、还要能够执行对应的‌工具，怎么实现这些呢？我们当然可以自主开发，不过还是更推荐使用 Spring AI、LangChai‎n 等开发框架。此外，有些 AI 大模型服务商也提‌供了对应的 SDK，都能够简化代码编写。

## Spring AI 工具开发

![image-20260323091553480](https://cdn.jsdelivr.net/gh/PHJ20030616/personal_pic/img/20260323091554741.png)

1. 工具定义与注册：Spring AI 可以通过简洁的注解自动生成工具定义和 JSON Schema，让 Java 方法轻松转变为 AI 可调用的工具。
2. 工具调用请求：Spring AI 自动处理与 AI 模型的通信并解析工具调用请求，并且支持多个工具链式调用。
3. 工具执行：Spring AI 提供统一的工具管理接口，自动根据 AI 返回的工具调用请求找到对应的工具并解析参数进行调用，让开发者专注于业务逻辑实现。
4. 处理工具结果：Spring AI 内置结果转换和异常处理机制，支持各种复杂 Java 对象作为返回值并优雅处理错误情况。
5. 返回结果给模型：Spring AI 封装响应结果并管理上下文，确保工具执行结果正确传递给模型或直接返回给用户。
6. 生成最终响应：Spring AI 自动整合工具调用结果到对话上下文，支持多轮复杂交互，确保 AI 回复的连贯性和准确性。

### 工具定义模式

![image-20260323092116021](https://cdn.jsdelivr.net/gh/PHJ20030616/personal_pic/img/20260323092116986.png)

1）Methods 模式：通过 `@Tool` 注解定义工具，通过 `tools` 方法绑定工具

```java
class WeatherTools {
    @Tool(description = "Get current weather for a location")
    public String getWeather(@ToolParam(description = "The city name") String city) {
        return "Current weather in " + city + ": Sunny, 25°C";
    }
}


ChatClient.create(chatModel)
    .prompt("What's the weather in Beijing?")
    .tools(new WeatherTools())
    .call();
```

2）Functions 模式：通过 `@Bean` 注解定义工具，通过 `functions` 方法绑定工具

```java
@Configuration
public class ToolConfig {
    @Bean
    @Description("Get current weather for a location")
    public Function<WeatherRequest, WeatherResponse> weatherFunction() {
        return request -> new WeatherResponse("Weather in " + request.getCity() + ": Sunny, 25°C");
    }
}


ChatClient.create(chatModel)
    .prompt("What's the weather in Beijing?")
    .functions("weatherFunction")
    .call();
```

### 定义工具

Spring AI 提供了两种定义工具的方法 —— **注解式** 和 **编程式**。

1）注解式：只需使用 `@Tool` 注解标记普通 Java 方法，就可以定义工具了，简单直观。

每个工具最好都添加详细清晰的描述，帮助 AI 理解何时应该调用这个工具。对于工具方法的参数，可以使用 `@ToolParam` 注解提供额外的描述信息和是否必填。

```
class WeatherTools {
    @Tool(description = "获取指定城市的当前天气情况")
    String getWeather(@ToolParam(description = "城市名称") String city) {
        
        return "北京今天晴朗，气温25°C";
    }
}
```

2）编程式⁠：如果想在运行时动‌态创建工具，可以选择编程式来定义工具，‎更灵活。

先定义工具类：

```
class WeatherTools {
    String getWeather(String city) {
        
        return "北京今天晴朗，气温25°C";
    }
}
```

然后将工具类⁠转换为 ToolCall‌back 工具定义类，之后就可以把这个类绑定给 ‎ChatClient，从‌而让 AI 使用工具了。

```
Method method = ReflectionUtils.findMethod(WeatherTools.class, "getWeather", String.class);
ToolCallback toolCallback = MethodToolCallback.builder()
    .toolDefinition(ToolDefinition.builder(method)
            .description("获取指定城市的当前天气情况")
            .build())
    .toolMethod(method)
    .toolObject(new WeatherTools())
    .build();
```

其实你会发⁠现，编程式就是把注‌解式的那些参数，改成通过调用方法来设置‎了而已。

在定义工具时，需要注⁠意方法参数和返回值类型的选择。Sprin‌g AI 支持大多数常见的 Java 类型作为参数和返回值，包括基本类型、复杂对象、‎集合等。而且返回值需要是可序列化的，‌因为它将被发送给 AI 大模型。

### 使用工具

定义好工具后⁠，Spring AI ‌提供了多种灵活的方式将工具提供给 ChatC‎lient，让 AI ‌能够在需要时调用这些工具。

1）按需使用：这是最简单的方式，直接在构建 ChatClient 请求时通过 `tools()` 方法附加工具。这种方式适合只在特定对话中使用某些工具的场景。

```
String response = ChatClient.create(chatModel)
    .prompt("北京今天天气怎么样？")
    .tools(new WeatherTools())  
    .call()
    .content();
```

2）全局使用：如⁠果某些工具需要在所有对话中都可用‌，可以在构建 ChatClient 时注册默认工具。这样，这些工‎具将对从同一个 ChatClie‌nt 发起的所有对话可用。

```
ChatClient chatClient = ChatClient.builder(chatModel)
    .defaultTools(new WeatherTools(), new TimeTools())  
    .build();
```

3）更底层的使用方⁠式：除了给 ChatClient ‌绑定工具外，也可以给更底层的 ChatModel 绑定工具（毕竟工具‎调用是 AI 大模型支持的能力），‌适合需要更精细控制的场景。

```
ToolCallback[] weatherTools = ToolCallbacks.from(new WeatherTools());

ChatOptions chatOptions = ToolCallingChatOptions.builder()
    .toolCallbacks(weatherTools)
    .build();

Prompt prompt = new Prompt("北京今天天气怎么样？", chatOptions);
chatModel.call(prompt);
```

总结一下，在使用工具时，Spring AI 会自动处理工具调用的全过程：从 AI 模型决定调用工具 => 到执行工具方法 => 再到将结果返回给模型 => 最后模型基于工具结果生成最终回答。这整个过程对开发者来说是透明的，我们只需专注于 **实现工具** 的业务逻辑即可。

### 工具生态

首先，工具的本质就是一种插件。能不自己写的插件，就尽量不要自己写。我们可以直接在网上找一些优秀的工具实现，比如 [Spring AI Alibaba 官方文档](https://java2ai.com/docs/1.0.0-M6.1/integrations/tools/) 中提到了社区插件。

虽然文档里只提到了屈指可数的插件数，但我们可以顺藤摸瓜，在 GitHub 社区找到官方提供的更多 [工具源码](https://github.com/alibaba/spring-ai-alibaba/tree/main/community/tool-calls)，包含大量有用的工具！比如翻译工具、网页搜索工具、爬虫工具、地图工具等

## 主流工具开发

如果社区中没找到合⁠适的工具，我们就要自主开发。需要注‌意的是，AI 自身能够实现的功能通常没必要定义为额外的工具，因为这会‎增加一次额外的交互，我们应该将工具‌用于 AI 无法直接完成的任务。

下面我们依次来实现需求分析中提到的 6 大工具，开发过程中我们要 **格外注意工具描述的定义**，因为它会影响 AI 决定是否使用工具。

先在项目根包下新建 `tools` 包，将所有工具类放在该包下；并且工具的返回值尽量使用 String 类型，让结果的含义更加明确。

### 文件操作

文件操作工具主要提供 2 大功能：保存文件、读取文件。

由于会影响系统资源，所以我们需要将文件统一存放到一个隔离的目录进行存储，在 `constant` 包下新建文件常量类，约定文件保存目录为项目根目录下的 `/tmp` 目录中。

```
public interface FileConstant {

    
    String FILE_SAVE_DIR = System.getProperty("user.dir") + "/tmp";
}

```

编写文件操作工具类，通过注解式定义工具，代码如下：

```
public class FileOperationTool {

    private final String FILE_DIR = FileConstant.FILE_SAVE_DIR + "/file";

    @Tool(description = "Read content from a file")
    public String readFile(@ToolParam(description = "Name of the file to read") String fileName) {
        String filePath = FILE_DIR + "/" + fileName;
        try {
            return FileUtil.readUtf8String(filePath);
        } catch (Exception e) {
            return "Error reading file: " + e.getMessage();
        }
    }

    @Tool(description = "Write content to a file")
    public String writeFile(
        @ToolParam(description = "Name of the file to write") String fileName,
        @ToolParam(description = "Content to write to the file") String content) {
        String filePath = FILE_DIR + "/" + fileName;
        try {
            
            FileUtil.mkdir(FILE_DIR);
            FileUtil.writeUtf8String(content, filePath);
            return "File written successfully to: " + filePath;
        } catch (Exception e) {
            return "Error writing to file: " + e.getMessage();
        }
    }
}

```

### 联网搜索

联网搜索工具的作用是根据关键词搜索网页列表。

我们可以使用专业的网页搜索 API，如 [Search API](https://www.searchapi.io/baidu) 来实现从多个网站搜索内容，这类服务通常按量计费。当然也可以直接使用 Google 或 Bing 的搜索 API（甚至是通过爬虫和网页解析从某个搜索引擎获取内容）。

阅读 Search API 的 [官方文档](https://www.searchapi.io/baidu)，重点关注 API 的请求参数和返回结果,把⁠接口文档喂给 AI‌，让它帮我们生成工具代码

### 网页抓取

网页抓取工具的作用是根据网址解析到网页的内容。

1）可以使⁠用 jsoup 库‌实现网页内容抓取和解析，首先给项目添‎加依赖：

```
<dependency>
    <groupId>org.jsoup</groupId>
    <artifactId>jsoup</artifactId>
    <version>1.19.1</version>
</dependency>

```

2）编写网页抓取工具类，几行代码就搞定了：

```
public class WebScrapingTool {

    @Tool(description = "Scrape the content of a web page")
    public String scrapeWebPage(@ToolParam(description = "URL of the web page to scrape") String url) {
        try {
            Document doc = Jsoup.connect(url).get();
            return doc.html();
        } catch (IOException e) {
            return "Error scraping web page: " + e.getMessage();
        }
    }
}
```

## **MCP 协议**

### 需求分析

目前我们的 AI  已经具备了恋爱知识问答以及调用工具的能力，现在让我们再加一个实用功能：根据用户的提问去拓展知识，学习RAG知识库中没有的一些知识

你会怎么实现呢？

按照我们之前学习的知识，应该能想到下面的思路：

1. 直接利用 AI 大模型自身的能力：大模型本身就有一定的训练知识，可以识别出知名的位置信息和约会地点，但是不够准确。
2. 利用 RAG 知识库：把约会地点整理成知识库，让 AI 利用它来回答，但是需要人工提供足够多的信息。
3. 利用工具调用：开发一个根据位置查询附近店铺的工具，可以利用第三方地图 API（比如高德地图 API）来实现，这样得到的信息更准确。

显然，第三种方⁠式的效果是最好的。但是既然‌要调用第三方 API，我们还需要手动开发工具么？为什‎么第三方 API 不能直接‌提供服务给我们的 AI 呢？

其实，已经有了！也就是我们今天的主角 —— MCP 协议。

### 什么是 MCP？

MCP（Model Co⁠ntext Protocol，模型上下文协议）是‌一种开放标准，目的是增强 AI 与外部系统的交互能力。MCP 为 AI 提供了与外部工具、资源和‎服务交互的标准化方式，让 AI 能够访问最新数据‌、执行复杂操作，并与现有系统集成。

根据 [官方定义](https://modelcontextprotocol.io/introduction)，MCP 是一种开放协议，它标准化了应用程序如何向大模型提供上下文的方式。可以将 MCP 想象成 AI 应用的 USB 接口。就像 USB 为设备连接各种外设和配件提供了标准化方式一样，MCP 为 AI 模型连接不同的数据源和工具提供了标准化的方法。

前面说的可能有些抽象，让我举些例子帮大家理解 MCP 的作用。首先是 **增强 AI 的能力**，通过 MCP 协议，AI 应用可以轻松接入别人提供的服务来实现更多功能，比如搜索网页、查询数据库、调用第三方 API、执行计算。

其次，我们一定要记住 MCP 它是个 **协议** 或者 **标准**，它本身并不提供什么服务，只是定义好了一套规范，让服务提供者和服务使用者去遵守。这样的好处显而易见，就像 HTTP 协议一样，现在前端向后端发送请求基本都是用 HTTP 协议，什么 get / post 请求类别、什么 401、404 状态码，这些标准能 **有效降低开发者的理解成本**。

此外，标准化还有其他的好处。举个例子，以前⁠我们想给 AI 增加查询地图的能力，需要自己开发工具来调用第三方地图 API；如果‌你有多个项目、或者其他开发者也需要做同样的能力，大家就要重复开发，就导致同样的功能做了多遍、每个人开发的质量和效果也会有差别。而如果官方把查询地图的能力直接做成一个‎服务，谁要用谁接入，不就省去了开发成本、并且效果一致了么？如果大家都陆续开放自己的‌服务，不就相当于打造了一个服务市场，造福广大开发者了么！

**标准可以造就生态。** 其实这并不新鲜了，前端同学可以想想 NPM 包，后端同学可以想想 Maven 仓库还有 Docker 镜像源，不懂编程的同学想想手机应用市场，应该就能理解了。

这就是 MCP 的三大作用：

- 轻松增强 AI 的能力
- 统一标准，降低使用和理解成本
- 打造服务生态，造福广大开发者

### MCP 架构

#### 宏观架构

MCP 的核心是 “⁠客户端 - 服务器” 架构，其中 MCP‌ 客户端主机可以连接到多个服务器。客户端主机是指希望访问 MCP 服务的程序，比‎如 Claude Desktop、IDE‌、AI 工具或部署在服务器上的项目。

![image-20260323125506124](https://cdn.jsdelivr.net/gh/PHJ20030616/personal_pic/img/20260323125507900.png)

#### SDK 3 层架构

如果我们要在程序中使用 MCP 或开发 MCP 服务，可以引入 MCP 官方的 SDK，比如 [Java SDK](https://modelcontextprotocol.io/sdk/java/mcp-overview)。让我们先通过 MCP 官方文档了解 MCP SDK 的架构，主要分为 3 层：

![image-20260323125545249](./assets/image-20260323125545249.png)

分别来看每一层的作用：

- 客户端 / 服务器层：McpClient 处理客户端操作，而 McpServer 管理服务器端协议操作。两者都使用 McpSession 进行通信管理。
- 会话层（McpSession）：通过 DefaultMcpSession 实现管理通信模式和状态。
- 传输层（McpTransport）：处理 JSON-RPC 消息序列化和反序列化，支持多种传输实现，比如 Stdio 标准 IO 流传输和 HTTP SSE 远程传输。

客户端和服⁠务端需要先经过下面‌的流程建立连接，之后才能正常交换消息‎：

![image-20260323125643255](https://cdn.jsdelivr.net/gh/PHJ20030616/personal_pic/img/20260323125644335.png)

#### MCP 客户端

MCP Client 是⁠ MCP 架构中的关键组件，主要负责和 MCP‌ 服务器建立连接并进行通信。它能自动匹配服务器的协议版本、确认可用功能、负责数据传输和 JS‎ON-RPC 交互。此外，它还能发现和使用各种‌工具、管理资源、和提示词系统进行交互。

除了这些核心功⁠能，MCP 客户端还支持一‌些额外特性，比如根管理、采样控制，以及同步或异步操作‎。为了适应不同场景，它提供‌了多种数据传输方式，包括：

- Stdio 标准输入 / 输出：适用于本地调用
- 基于 Java HttpClient 和 WebFlux 的 SSE 传输：适用于远程调用

客户端可以⁠通过不同传输方式调‌用不同的 MCP 服务，可以是本地的‎、也可以是远程的。‌

#### MCP 服务端

MCP S⁠erver 也是整‌个 MCP 架构的关键组件，主要用来‎为客户端提供各种工‌具、资源和功能支持。

它负责处理客户端⁠的请求，包括解析协议、提供工具‌、管理资源以及处理各种交互信息。同时，它还能记录日志、发送通‎知，并且支持多个客户端同时连接‌，保证高效的通信和协作。

和客户端一样，它也⁠可以通过多种方式进行数据传输，比如‌ Stdio 标准输入 / 输出、基于 Servlet / WebF‎lux / WebMVC 的 SS‌E 传输，满足不同应用场景。

这种设计使⁠得客户端和服务端完‌全解耦，任何语言开发的客户端都可以调‎用 MCP 服务。‌如图：

![image-20260323125853952](./assets/image-20260323125853952.png)

## 使用 MCP

 3 种使用 MCP 的方式：

- 云平台使用 MCP
- 软件客户端使用 MCP
- 程序中使用 MCP

无论是哪种使用方式，原理都是类似的，而且有 2 种可选的使用模式：**本地下载 MCP 服务端代码并运行**（类似引入了一个 SDK），或者 **直接使用已部署的 MCP 服务**（类似调用了别人的 API）。

到哪里去找别人开发的 MCP 服务呢？

### MCP 服务大全

目前已经有⁠很多 MCP 服务‌市场，开发者可以在这些平台上找到各种‎现成的 MCP 服‌务：

- [MCP.so](https://mcp.so/)：较为主流，提供丰富的 MCP 服务目录
- [GitHub Awesome MCP Servers](https://github.com/punkpeye/awesome-mcp-servers)：开源 MCP 服务集合
- [阿里云百炼 MCP 服务市场](https://bailian.console.aliyun.com/?tab=mcp#/mcp-market)
- [Spring AI Alibaba 的 MCP 服务市场](https://java2ai.com/mcp/)
- [Glama.ai MCP 服务](https://glama.ai/mcp/servers)

其中，绝大多⁠数 MCP 服务市场仅‌提供本地下载 MCP 服务端代码并运行的使用‎方式，毕竟部署 MCP‌ 服务也是需要成本的。

有些云服务平台提⁠供了云端部署的 MCP 服务，比‌如阿里云百炼平台，在线填写配置后就能用，可以轻松和平台上的 AI 应‎用集成。但一般局限性也比较大‌，不太能直接在自己的代码中使用。

## MCP服务开发

### MCP 服务端开发

1）在项目⁠根目录下新建 mo‌dule，名称为 J-image-search-server-mcp	

注意，建议在新项目中 **单独打开该模块**，不要直接在原项目的子文件夹中操作，否则可能出现路径上的问题。

2）引入必⁠要的依赖，包括 L‌ombok、hutool 工具库和 ‎Spring AI‌ MCP 服务端依赖。

有 Stdio、⁠WebMVC SSE 和 Web‌Flux SSE 三种服务端依赖可以选择，开发时只需要填写不同的‎配置，开发流程都是一样的。此处我‌们选择引入 WebMVC：

```
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-mcp-server-webmvc-spring-boot-starter</artifactId>
    <version>1.0.0-M6</version>
</dependency>
```

3）在 re⁠sources 目录下‌编写服务端配置文件。这里我们编写两套配置方案‎，分别实现 stdio‌ 和 SSE 模式的传输。

stdio 配置文件 `application-stdio.yml`（需关闭 web 支持）：

```
spring:
  application:
    name: J-image-search-server-mcp
  ai:
    mcp:
      server:
        name: J-image-search-server-mcp
        version: 0.0.1
        type: SYNC
        # stdio
        stdio: true
  # stdio
  main:
    web-application-type: none
    banner-mode: off
```

SSE 配置文件 `application-sse.yml`（需关闭 stdio 模式）：

```
spring:
  ai:
    mcp:
      server:
        name: J-image-search-server-mcp
        version: 0.0.1
        type: SYNC
        # sse
        stdio: false

```

然后编写主配置文件 `application.yml`，可以灵活指定激活哪套配置：

```
spring:
  application:
    name: J-image-search-server-mcp
  profiles:
    active: stdio
server:
  port: 8127
```

4）编写图片搜索服务类，在 `tools` 包下新建 ImageSearchTool，使用 `@Tool` 注解标注方法，作为 MCP 服务提供的工具。（这个代码可以使用AI生成--将官方文档喂给AI，让他自己编写代码）

```
package com.phj.jimagesearchservermcp.tools;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ImageSearchTool {

    // 替换为你的 Pexels API 密钥（需从官网申请）
    private static final String API_KEY = "改为你的 API Key";

    // Pexels 常规搜索接口（请以文档为准）
    private static final String API_URL = "https://api.pexels.com/v1/search";

    @Tool(description = "search image from web")
    public String searchImage(@ToolParam(description = "Search query keyword") String query) {
        try {
            return String.join(",", searchMediumImages(query));
        } catch (Exception e) {
            return "Error search image: " + e.getMessage();
        }
    }

    /**
     * 搜索中等尺寸的图片列表
     *
     * @param query
     * @return
     */
    public List<String> searchMediumImages(String query) {
        // 设置请求头（包含API密钥）
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", API_KEY);

        // 设置请求参数（仅包含query，可根据文档补充page、per_page等参数）
        Map<String, Object> params = new HashMap<>();
        params.put("query", query);

        // 发送 GET 请求
        String response = HttpUtil.createGet(API_URL)
                .addHeaders(headers)
                .form(params)
                .execute()
                .body();

        // 解析响应JSON（假设响应结构包含"photos"数组，每个元素包含"medium"字段）
        return JSONUtil.parseObj(response)
                .getJSONArray("photos")
                .stream()
                .map(photoObj -> (JSONObject) photoObj)
                .map(photoObj -> photoObj.getJSONObject("src"))
                .map(photo -> photo.getStr("medium"))
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toList());
    }
}

```

5）在主类中通过定义 `ToolCallbackProvider` Bean 来注册工具：

```
package com.phj.jimagesearchservermcp;

import com.phj.jimagesearchservermcp.tools.ImageSearchTool;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class JImageSearchServerMcpApplication {

    public static void main(String[] args) {
        SpringApplication.run(JImageSearchServerMcpApplication.class, args);
    }

    @Bean
    public ToolCallbackProvider imageSearchTools(ImageSearchTool imageSearchTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(imageSearchTool)
                .build();
    }

}

```

### 客户端开发

接下来直接⁠在根项目中开发客户‌端，调用刚才创建的图片搜索服务。

1）先引入必要的 MCP 客户端依赖

```
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-mcp-client-spring-boot-starter</artifactId>
    <version>1.0.0-M6</version>
</dependency>
```

2）先测试 stdio 传输方式。在 `mcp-servers.json` 配置文件中新增 MCP Server 的配置，通过 java 命令执行我们刚刚打包好的 jar 包。代码如下：

```
"J-image-search-server-mcp": {
      "command": "java",
      "args": [
        "-Dspring.ai.mcp.server.stdio=true",
        "-Dspring.main.web-application-type=none",
        "-Dlogging.pattern.console=",
        "-jar",
        "J-image-search-server-mcp/target/J-image-search-server-mcp-0.0.1-SNAPSHOT.jar"
      ],
      "env": {}
    }
```

3）接下来⁠测试 SSE 连接‌方式，首先修改 MCP 服务端的配置‎文件，激活 SSE‌ 的配置：

```
spring:
  application:
    name: J-image-search-server-mcp
  profiles:
    active: sse
server:
  port: 8127
```

然后以 Debug 模式启动 MCP 服务。

然后修改客⁠户端的配置文件，添‌加 SSE 配置，同时要注释原有的 ‎stdio 配置以‌避免端口冲突：

```
spring:
  ai:
    mcp:
      client:
        sse:
          connections:
            server1:
              url: http://localhost:8127
```

## MCP 开发最佳实践

1）慎用 MCP：MCP 不是银弹，其本质就是工具调用，只不过统一了标准、更容易共享而已。如果我们自己开发一些不需要共享的工具，完全没必要使用 MCP，可以节约开发和部署成本。我个人的建议是 **能不用就不用**，先开发工具调用，之后需要提供 MCP 服务时再将工具调用转换成 MCP 服务即可。

2）传输模式选择：⁠Stdio 模式作为客户端子进程运行‌，无需网络传输，因此安全性和性能都更高，更适合小型项目；SSE 模式适合‎作为独立服务部署，可以被多客户端共享‌调用，更适合模块化的中大型项目团队。

3）明确服务：设计 MCP 服务时，要合理划分工具和资源，并且利用 `@Tool`、`@ToolParam` 注解尽可能清楚地描述工具的作用，便于 AI 理解和选择调用。

4）注意容错⁠：和工具开发一样，要注意‌ MCP 服务的容错性和健壮性，捕获并处理所有可‎能的异常，并且返回友好的‌错误信息，便于客户端处理。

5）性能优化：MCP 服⁠务端要防止单次执行时间过长，可以采用异步模式来‌处理耗时操作，或者设置超时时间，客户端也要合理设置超时时间，防‎止因为 MCP 调用时间过长而导致 AI 应用‌阻塞                

6）跨平台兼容性：开发 MCP 服务时，应该考虑在 Windows、Linux 和 macOS 等不同操作系统上的兼容性。特别是使用 stdio 传输模式时，注意路径分隔符差异、进程启动方式和环境变量设置。比如客户端在 Windows 系统中使用命令时需要额外添加 `.cmd` 后缀。

## **AI 智能体构建**

### 智能体的分类

跟人的生长⁠阶段一样，智能体也‌是可以不断进化的。按照自主性和规划能‎力，智能体可以分为‌几个层次：

1）反应式智能⁠体：仅根据当前输入和固定规则‌做出反应，类似简单的聊天机器人，没有真正的规划能力。23‎ 年时的大多数 AI 聊天机‌器人应用，几乎都是反应式智能体。

2）有限规划智能体：能进⁠行简单地多步骤执行，但执行路径通常是预设的或有‌严格限制的。鉴定为 “能干事、但干不了复杂的大事”。24 年流行的很多可联网搜索内容、调用知‎识库和工具的 AI 应用，都属于这类智能体。比‌如 ChatGPT + Plugins

3）自主规⁠划智能体：也叫目标导‌向智能体，能够根据任务目标自主分解任务、‎制定计划、选择工具并‌一步步执行，直到完成任务。

比如 25 年初很火的 M⁠anus 项目，它的核心亮点在于其 “自主执行” 能‌力。据官方介绍，Manus 能够在虚拟机中调用各种工具（如编写代码、爬取数据）完成任务。其应用场景‎覆盖旅行规划、股票分析、教育内容生成等 40 余个‌领域，所以在当时给了很多人震撼感。

但其实早在这之前，就有类似的项目了，比如 AutoGPT，所以 Manus 大火的同时也被人诟病 “会营销而已”。甚至没隔多久就有小团队开源了 Manus 的复刻版 —— [OpenManus](https://github.com/FoundationAgents/OpenManus)，这类智能体通过 “思考 - 行动 - 观察” 的循环模式工作，能够持续推进任务直至完成目标。

### 智能体实现关键技术

#### CoT 思维链

CoT（Chain of⁠ Thought）思维链是一种让 AI 像人类一‌样 “思考” 的技术，帮助 AI 在处理复杂问题时能够按步骤思考。对于复杂的推理类问题，先思考后‎执行，效果往往更好。而且还可以让模型在生成答案时‌展示推理过程，便于我们理解和优化 AI。

CoT 的实现方式其实很简单⁠，可以在输入 Prompt 时，给模型提供额外的提示或‌引导，比如 “让我们一步一步思考这个问题”，让模型以逐步推理的方式生成回答。还可以运用 Prompt 的优化‎技巧 few shot，给模型提供包含思维链的示例问题‌和答案，让模型学习如何构建自己的思维链。

在 Ope⁠nManus 早期‌版本中，可以看到实现 CoT 的系统‎提示词：

```
You are an assistant focused on Chain of Thought reasoning. For each question, please follow these steps:  
  
1. Break down the problem: Divide complex problems into smaller, more manageable parts  
2. Think step by step: Think through each part in detail, showing your reasoning process  
3. Synthesize conclusions: Integrate the thinking from each part into a complete solution  
4. Provide an answer: Give a final concise answer  
  
Your response should follow this format:  
Thinking: [Detailed thought process, including problem decomposition, reasoning for each step, and analysis]  
Answer: [Final answer based on the thought process, clear and concise]  
  
Remember, the thinking process is more important than the final answer, as it demonstrates how you reached your conclusion.
```

#### Agent Loop 执行循环

Agent⁠ Loop 是智能体‌最核心的工作机制，指智能体在没有用户输入‎的情况下，自主重复执‌行推理和工具调用的过程。

在传统的聊天模型中，⁠每次用户提问后，AI 回复一次就结束‌了。但在智能体中，AI 回复后可能会继续自主执行后续动作（如调用工具、处理结果、继续‎推理），形成一个自主执行的循环，直到任务‌完成（或者超出预设的最大步骤数）。

Agent Loop 的实现很简单，参考代码如下：

```
public String execute() {  
    List<String> results = new ArrayList<>();  
    while (currentStep < MAX_STEPS && !isFinished) {  
        currentStep++;  
        
        String stepResult = executeStep();  
        results.add("步骤 " + currentStep + ": " + stepResult);  
    }  
    if (currentStep >= MAX_STEPS) {  
        results.add("达到最大步骤数: " + MAX_STEPS);  
    }  
    return String.join("\n", results);  
}
```

#### ReAct 模式

ReAct（Reas⁠oning + Acting）是一种结合‌推理和行动的智能体架构，它模仿人类解决问题时 ” 思考 - 行动 - 观察” 的循‎环，目的是通过交互式决策解决复杂任务，是‌目前最常用的智能体工作模式之一。

核心思想：

1. 推理（Reason）：将原始问题拆分为多步骤任务，明确当前要执行的步骤。
2. 行动（Act）：调用外部工具执行动作，比如调用搜索引擎、打开浏览器访问网页等。
3. 观察（Observe）：获取工具返回的结果，反馈给智能体进行下一步决策。比如将打开的网页代码输入给 AI。
4. 循环迭代：不断重复上述 3 个过程，直到任务完成或达到终止条件。

ReAct 流程如图：

![image-20260323201158951](https://cdn.jsdelivr.net/gh/PHJ20030616/personal_pic/img/20260323201200142.png)

#### 所需支持系统

除了基本的工作机制外，智能体的实现还依赖于很多支持系统。

1）首先是 ⁠AI 大模型，这个就不‌多说了，大模型提供了思考、推理和决策的核心能‎力，越强的 AI 大模‌型通常执行任务的效果越好。

2）记忆系统

智能体需要记忆系统⁠来存储对话历史、中间结果和执行状态，‌这样它才能够进行连续对话并根据历史对话分析接下来的工作步骤。之前我们学习‎过如何使用 Spring AI 的 ‌ChatMemory 实现对话记忆。

3）知识库

尽管大语言模型拥有⁠丰富的参数知识，但针对特定领域的专‌业知识往往需要额外的知识库支持。之前我们学习过，通过 RAG 检索增‎强生成 + 向量数据库等技术，智能‌体可以检索并利用专业知识回答问题。

4）工具调用

工具是扩展智能体⁠能力边界的关键，智能体通过工具调‌用可以访问搜索引擎、数据库、API 接口等外部服务，极大地增强了‎其解决实际问题的能力。当然，MC‌P 也可以算是工具调用的一种。

## 看开源项目源码的方式

1. 看README
2. 运行代码，使用功能，尝试梳理业务流程
3. 看项目整体架构是如何组织的
4. 分块看不同功能，例如：MCP，RAG，Agent

## 自主实现智能体

### 定义数据模型

新建 `agent.model` 包，将所有用到的数据模型（实体类、枚举类等）都放到该包下。

目前我们只⁠需要定义 Agen‌t 的状态枚举，用于控制智能体的执行‎。AgentSta‌te 代码如下：

```
package com.phj.jaiagent.agent.model;

/**
 * 代理执行状态的枚举类
 */
public enum AgentState {

    /**
     * 空闲状态
     */
    IDLE,

    /**
     * 运行中状态
     */
    RUNNING,

    /**
     * 已完成状态
     */
    FINISHED,

    /**
     * 错误状态
     */
    ERROR
}
```

### 核⁠心架构开发

首先定义智能体的核心架构，包括以下类：

- BaseAgent：智能体基类，定义基本信息和多步骤执行流程
- ReActAgent：实现思考和行动两个步骤的智能体
- ToolCallAgent：实现工具调用能力的智能体
- JManus：最终可使用的 Manus 实例

#### 开发基础 Agent 类

参考 Op⁠enManus 的‌实现方式，BaseAgent 的代码‎如下：

```
package com.phj.jaiagent.agent;

import cn.hutool.core.util.StrUtil;
import com.phj.jaiagent.agent.model.AgentState;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 抽象基础代理类，用于管理代理状态和执行流程。
 * <p>
 * 提供状态转换、内存管理和基于步骤的执行循环的基础功能。
 * 子类必须实现step方法。
 */
@Data
@Slf4j
public abstract class BaseAgent {

    // 核心属性
    private String name;

    // 提示词
    private String systemPrompt;
    private String nextStepPrompt;

    // 代理状态
    private AgentState state = AgentState.IDLE;

    // 执行步骤控制
    private int currentStep = 0;
    private int maxSteps = 10;

    // LLM 大模型
    private ChatClient chatClient;

    // Memory 记忆（需要自主维护会话上下文）
    private List<Message> messageList = new ArrayList<>();

    /**
     * 运行代理
     *
     * @param userPrompt 用户提示词
     * @return 执行结果
     */
    public String run(String userPrompt) {
        // 1、基础校验
        if (this.state != AgentState.IDLE) {
            throw new RuntimeException("Cannot run agent from state: " + this.state);
        }
        if (StrUtil.isBlank(userPrompt)) {
            throw new RuntimeException("Cannot run agent with empty user prompt");
        }
        // 2、执行，更改状态
        this.state = AgentState.RUNNING;
        // 记录消息上下文
        messageList.add(new UserMessage(userPrompt));
        // 保存结果列表
        List<String> results = new ArrayList<>();
        try {
            // 执行循环
            for (int i = 0; i < maxSteps && state != AgentState.FINISHED; i++) {
                int stepNumber = i + 1;
                currentStep = stepNumber;
                log.info("Executing step {}/{}", stepNumber, maxSteps);
                // 单步执行
                String stepResult = step();
                String result = "Step " + stepNumber + ": " + stepResult;
                results.add(result);
            }
            // 检查是否超出步骤限制
            if (currentStep >= maxSteps) {
                state = AgentState.FINISHED;
                results.add("Terminated: Reached max steps (" + maxSteps + ")");
            }
            return String.join("\n", results);
        } catch (Exception e) {
            state = AgentState.ERROR;
            log.error("error executing agent", e);
            return "执行错误" + e.getMessage();
        } finally {
            // 3、清理资源
            this.cleanup();
        }
    }

    /**
     * 运行代理（流式输出）
     *
     * @param userPrompt 用户提示词
     * @return 执行结果
     */
    public SseEmitter runStream(String userPrompt) {
        // 创建一个超时时间较长的 SseEmitter
        SseEmitter sseEmitter = new SseEmitter(300000L); // 5 分钟超时
        // 使用线程异步处理，避免阻塞主线程
        CompletableFuture.runAsync(() -> {
            // 1、基础校验
            try {
                if (this.state != AgentState.IDLE) {
                    sseEmitter.send("错误：无法从状态运行代理：" + this.state);
                    sseEmitter.complete();
                    return;
                }
                if (StrUtil.isBlank(userPrompt)) {
                    sseEmitter.send("错误：不能使用空提示词运行代理");
                    sseEmitter.complete();
                    return;
                }
            } catch (Exception e) {
                sseEmitter.completeWithError(e);
            }
            // 2、执行，更改状态
            this.state = AgentState.RUNNING;
            // 记录消息上下文
            messageList.add(new UserMessage(userPrompt));
            // 保存结果列表
            List<String> results = new ArrayList<>();
            try {
                // 执行循环
                for (int i = 0; i < maxSteps && state != AgentState.FINISHED; i++) {
                    int stepNumber = i + 1;
                    currentStep = stepNumber;
                    log.info("Executing step {}/{}", stepNumber, maxSteps);
                    // 单步执行
                    String stepResult = step();
                    String result = "Step " + stepNumber + ": " + stepResult;
                    results.add(result);
                    // 输出当前每一步的结果到 SSE
                    sseEmitter.send(result);
                }
                // 检查是否超出步骤限制
                if (currentStep >= maxSteps) {
                    state = AgentState.FINISHED;
                    results.add("Terminated: Reached max steps (" + maxSteps + ")");
                    sseEmitter.send("执行结束：达到最大步骤（" + maxSteps + "）");
                }
                // 正常完成
                sseEmitter.complete();
            } catch (Exception e) {
                state = AgentState.ERROR;
                log.error("error executing agent", e);
                try {
                    sseEmitter.send("执行错误：" + e.getMessage());
                    sseEmitter.complete();
                } catch (IOException ex) {
                    sseEmitter.completeWithError(ex);
                }
            } finally {
                // 3、清理资源
                this.cleanup();
            }
        });

        // 设置超时回调
        sseEmitter.onTimeout(() -> {
            this.state = AgentState.ERROR;
            this.cleanup();
            log.warn("SSE connection timeout");
        });
        // 设置完成回调
        sseEmitter.onCompletion(() -> {
            if (this.state == AgentState.RUNNING) {
                this.state = AgentState.FINISHED;
            }
            this.cleanup();
            log.info("SSE connection completed");
        });
        return sseEmitter;
    }

    /**
     * 定义单个步骤
     *
     * @return
     */
    public abstract String step();

    /**
     * 清理资源
     */
    protected void cleanup() {
        // 子类可以重写此方法来清理资源
    }
}

```

上述代码中，我们要注意 3 点：

1. 包含 chatClient 属性，由调用方传入具体调用大模型的对象，而不是写死使用的大模型，更灵活
2. 包含 messageList 属性，用于维护消息上下文列表
3. 通过 state 属性来控制智能体的执行流程

#### 开发 ReActAgent 类

参考 OpenM⁠anus 的实现方式，继承自 ‌BaseAgent，并且将 step 方法分解为 think‎ 和 act 两个抽象方法。R‌eActAgent 的代码如下：

```
package com.phj.jaiagent.agent;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

/**
 * ReAct (Reasoning and Acting) 模式的代理抽象类
 * 实现了思考-行动的循环模式
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public abstract class ReActAgent extends BaseAgent {

    /**
     * 处理当前状态并决定下一步行动
     *
     * @return 是否需要执行行动，true表示需要执行，false表示不需要执行
     */
    public abstract boolean think();

    /**
     * 执行决定的行动
     *
     * @return 行动执行结果
     */
    public abstract String act();

    /**
     * 执行单个步骤：思考和行动
     *
     * @return 步骤执行结果
     */
    @Override
    public String step() {
        try {
            // 先思考
            boolean shouldAct = think();
            if (!shouldAct) {
                return "思考完成 - 无需行动";
            }
            // 再行动
            return act();
        } catch (Exception e) {
            // 记录异常日志
            e.printStackTrace();
            return "步骤执行失败：" + e.getMessage();
        }
    }

}

```

#### 开发 ToolCallAgent 类

ToolCa⁠llAgent 负责实现‌工具调用能力，继承自 ReActAgent，具体‎实现了 think 和 ‌act 两个抽象方法。

我们有 3 种方案来实现 ToolCallAgent：

1）基于 ⁠Spring AI‌ 的工具调用能力，手动控制工具执行。

其实 Spring 的 ChatClient 已经支持选择工具进行调用（内部完成了 think、act、observe），但这里我们要自主实现，可以使用 Spring AI 提供的 [手动控制工具执行](https://docs.spring.io/spring-ai/reference/api/tools.html#_user_controlled_tool_execution)。

2）基于 ⁠Spring AI‌ 的工具调用能力，简化调用流程。

由于 Spr⁠ing AI 完全托管了‌工具调用，我们可以直接把所有工具调用的代码作为 ‎think 方法，而 a‌ct 方法不定义任何动作。

3）自主实现工具调用能力。

也就是工具调用⁠章节提到的实现原理：自己写‌ Prompt，引导 AI 回复想要调用的工具列表和‎调用参数，然后再执行工具并‌将结果返送给 AI 再次执行。

使用哪种方案呢？

如果是为了学⁠习 ReAct 模式，让‌流程更清晰，推荐第一种；如果只是为了快速实现，推‎荐第二种；不建议采用第三‌种方案，过于原生，开发成本高。

下面我们采⁠用第一种方案实现 ‌ToolCallAgent，先定义所‎需的属性和构造方法‌：

```
package com.phj.jaiagent.agent;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.phj.jaiagent.agent.model.AgentState;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallback;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 处理工具调用的基础代理类，具体实现了 think 和 act 方法，可以用作创建实例的父类
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class ToolCallAgent extends ReActAgent {

    // 可用的工具
    private final ToolCallback[] availableTools;

    // 保存工具调用信息的响应结果（要调用那些工具）
    private ChatResponse toolCallChatResponse;

    // 工具调用管理者
    private final ToolCallingManager toolCallingManager;

    // 禁用 Spring AI 内置的工具调用机制，自己维护选项和消息上下文
    private final ChatOptions chatOptions;

    public ToolCallAgent(ToolCallback[] availableTools) {
        super();
        this.availableTools = availableTools;
        this.toolCallingManager = ToolCallingManager.builder().build();
        // 禁用 Spring AI 内置的工具调用机制，自己维护选项和消息上下文
        this.chatOptions = DashScopeChatOptions.builder()
                .withInternalToolExecutionEnabled(false)
                .build();
    }

    /**
     * 处理当前状态并决定下一步行动
     *
     * @return 是否需要执行行动
     */
    @Override
    public boolean think() {
        // 1、校验提示词，拼接用户提示词
        if (StrUtil.isNotBlank(getNextStepPrompt())) {
            UserMessage userMessage = new UserMessage(getNextStepPrompt());
            getMessageList().add(userMessage);
        }
        // 2、调用 AI 大模型，获取工具调用结果
        List<Message> messageList = getMessageList();
        Prompt prompt = new Prompt(messageList, this.chatOptions);
        try {
            ChatResponse chatResponse = getChatClient().prompt(prompt)
                    .system(getSystemPrompt())
                    .tools(availableTools)
                    .call()
                    .chatResponse();
            // 记录响应，用于等下 Act
            this.toolCallChatResponse = chatResponse;
            // 3、解析工具调用结果，获取要调用的工具
            // 助手消息
            AssistantMessage assistantMessage = chatResponse.getResult().getOutput();
            // 获取要调用的工具列表
            List<AssistantMessage.ToolCall> toolCallList = assistantMessage.getToolCalls();
            // 输出提示信息
            String result = assistantMessage.getText();
            log.info(getName() + "的思考：" + result);
            log.info(getName() + "选择了 " + toolCallList.size() + " 个工具来使用");
            String toolCallInfo = toolCallList.stream()
                    .map(toolCall -> String.format("工具名称：%s，参数：%s", toolCall.name(), toolCall.arguments()))
                    .collect(Collectors.joining("\n"));
            log.info(toolCallInfo);
            // 如果不需要调用工具，返回 false
            if (toolCallList.isEmpty()) {
                // 只有不调用工具时，才需要手动记录助手消息
                getMessageList().add(assistantMessage);
                return false;
            } else {
                // 需要调用工具时，无需记录助手消息，因为调用工具时会自动记录
                return true;
            }
        } catch (Exception e) {
            log.error(getName() + "的思考过程遇到了问题：" + e.getMessage());
            getMessageList().add(new AssistantMessage("处理时遇到了错误：" + e.getMessage()));
            return false;
        }
    }

    /**
     * 执行工具调用并处理结果
     *
     * @return 执行结果
     */
    @Override
    public String act() {
        if (!toolCallChatResponse.hasToolCalls()) {
            return "没有工具需要调用";
        }
        // 调用工具
        Prompt prompt = new Prompt(getMessageList(), this.chatOptions);
        ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(prompt, toolCallChatResponse);
        // 记录消息上下文，conversationHistory 已经包含了助手消息和工具调用返回的结果
        setMessageList(toolExecutionResult.conversationHistory());
        ToolResponseMessage toolResponseMessage = (ToolResponseMessage) CollUtil.getLast(toolExecutionResult.conversationHistory());
        // 判断是否调用了终止工具
        boolean terminateToolCalled = toolResponseMessage.getResponses().stream()
                .anyMatch(response -> response.name().equals("doTerminate"));
        if (terminateToolCalled) {
            // 任务结束，更改状态
            setState(AgentState.FINISHED);
        }
        String results = toolResponseMessage.getResponses().stream()
                .map(response -> "工具 " + response.name() + " 返回的结果：" + response.responseData())
                .collect(Collectors.joining("\n"));
        log.info(results);
        return results;
    }
}

```

#### 开发 JManus 类

JManus 是⁠可以直接提供给其他方法调用的 AI‌ 超级智能体实例，继承自 ToolCallAgent，需要给智能体设‎置各种参数，比如对话客户端 cha‌tClient、工具调用列表等。

代码如下：

```
package com.phj.jaiagent.agent;

import com.phj.jaiagent.advisor.MyLoggerAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;

/**
 * 鱼皮的 AI 超级智能体（拥有自主规划能力，可以直接使用）
 */
@Component
public class JManus extends ToolCallAgent {

    public JManus(ToolCallback[] allTools, ChatModel dashscopeChatModel) {
        super(allTools);
        this.setName("JManus");
        String SYSTEM_PROMPT = """
                You are JManus, an all-capable AI assistant, aimed at solving any task presented by the user.
                You have various tools at your disposal that you can call upon to efficiently complete complex requests.
                """;
        this.setSystemPrompt(SYSTEM_PROMPT);
        String NEXT_STEP_PROMPT = """
                Based on user needs, proactively select the most appropriate tool or combination of tools.
                For complex tasks, you can break down the problem and use different tools step by step to solve it.
                After using each tool, clearly explain the execution results and suggest the next steps.
                If you want to stop the interaction at any point, use the `terminate` tool/function call.
                """;
        this.setNextStepPrompt(NEXT_STEP_PROMPT);
        this.setMaxSteps(20);
        // 初始化 AI 对话客户端
        ChatClient chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultAdvisors(new MyLoggerAdvisor())
                .build();
        this.setChatClient(chatClient);
    }
}

```

## AI 应用接口开发

我们平时开发的大多数接口都⁠是同步接口，也就是等后端处理完再返回。但是对于 A‌I 应用，特别是响应时间较长的对话类应用，可能会让用户失去耐心等待，因此推荐使用 SSE（Serve‎r-Sent Events）技术实现实时流式输出，‌类似打字机效果，大幅提升用户体验。

接下来我们⁠会同时提供同步接口‌（一次性完整返回）和基于 SSE 的‎流式输出接口。

### 开发

#### 支持流式调用

首先，我们⁠需要为 KnowledgeAp‌p 添加流式调用方法，通过 stream‎ 方法就可以返回 F‌lux 响应式对象了：

```
    /**
     * AI 基础对话（支持多轮对话记忆，SSE 流式传输）
     *
     * @param message
     * @param chatId
     * @return
     */
    public Flux<String> doChatByStream(String message, String chatId) {
        return chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .stream()
                .content();
    }
```

#### 开发同步接口

在 controller 包下新建 `AiController`，将所有的接口都写在这个文件内。

```
/**
     * 同步调用 AI  应用
     *
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping("/knowledge_app/chat/sync")
    public String doChatWithknowledgeAppSync(String message, String chatId) {
        return knowledgeApp.doChat(message, chatId);
    }
```

#### 开发 SSE 流式接口

然后编写基于⁠ SSE 的流式输出接‌口，有几种常见的实现方式：         ‎           ‌            

1） 返回⁠ Flux 响应式‌对象，并且添加 SSE 对应的 Me‎diaType：

```
/**
     * SSE 流式调用 AI  应用
     *
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping(value = "/knowledge_app/chat/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatWithknowledgeAppSSE(String message, String chatId) {
        return knowledgeApp.doChatByStream(message, chatId);
    }
```

2）返回 ⁠Flux 对象，并且‌设置泛型为 ServerSentEv‎ent。使用这种方式可以‌省略 MediaType：

```
/**
     * SSE 流式调用 AI  应用
     *
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping(value = "/knowledge_app/chat/server_sent_event")
    public Flux<ServerSentEvent<String>> doChatWithknowledgeAppServerSentEvent(String message, String chatId) {
        return knowledgeApp.doChatByStream(message, chatId)
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)
                        .build());
    }
```

3）使用 ⁠SSEEmiter，‌通过 send 方法持续向 SseEmi‎tter 发送消息（‌有点像 IO 操作）：

```
/**
     * SSE 流式调用 AI  应用
     *
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping(value = "/knowledge_app/chat/sse_emitter")
    public SseEmitter doChatWithknowledgeAppServerSseEmitter(String message, String chatId) {
        // 创建一个超时时间较长的 SseEmitter
        SseEmitter sseEmitter = new SseEmitter(180000L); // 3 分钟超时
        // 获取 Flux 响应式数据流并且直接通过订阅推送给 SseEmitter
        knowledgeApp.doChatByStream(message, chatId)
                .subscribe(chunk -> {
                    try {
                        sseEmitter.send(chunk);
                    } catch (IOException e) {
                        sseEmitter.completeWithError(e);
                    }
                }, sseEmitter::completeWithError, sseEmitter::complete);
        // 返回
        return sseEmitter;
    }
```

