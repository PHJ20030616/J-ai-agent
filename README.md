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
public class LoveApp {

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


    public LoveApp(ChatModel dashscopeChatModel) {
        
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
    VectorStore loveAppVectorStore(EmbeddingModel dashscopeEmbeddingModel) {
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
//                .advisors(loveAppRagCloudAdvisor)
//                 应用 RAG 检索增强服务（基于 PgVector 向量存储）
//                .advisors(new QuestionAnswerAdvisor(pgVectorVectorStore))
                // 应用自定义的 RAG 检索增强服务（文档查询器 + 上下文增强器）
//                .advisors(
//                        LoveAppRagCustomAdvisorFactory.createLoveAppRagCustomAdvisor(
//                                loveAppVectorStore, "单身"
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
