# RAG（二）

在前文大致了解了离线阶段做的事情：https://mp.weixin.qq.com/s/NwxuHEkQZiHsYKHiddtl8A

本文再来简单看看在线阶段是如何做的。

## 1. 流程介绍

![](D:\my_note\aa_md\txf-node\aiimg\rr1.svg)

从上图可以看到整个在线的过程还是很长的。

第一步是<span style="background-color: yellow">**针对用户的输入**</span>：不要直接拿用户的原始话去检索。用户的话通常有口语化、指代词（如“它怎么用”）、错别字等问题。

**用户输入的意图识别**：简单分类（知识问答 vs 闲聊 vs 敏感词）。如果是闲聊直接拦截走LLM，不浪费检索资源。

然后查询重写：需要将用户的当前问题 + 最近几轮对话历史，重写成一个独立、完整、适合检索的Query（当然也可以把这轮对话的历史记录一股脑给模型）。

第二步是<span style="background-color: yellow">**混合检索**</span>：

**向量检索**：擅长语义匹配（如“人工智障”能匹配到“人工智能”），但不擅长专有名词、编号精准匹配。

**稀疏检索 (BM25/Keyword)**：擅长精准匹配（如匹配特定订单号“PO-123456”、特定人名）。

对于上面两种检索做**双路召回**：同时走Milvus的Dense向量检索 和 BM25检索（可以用ES，或者Milvus 2.4+原生支持的Sparse Vector）。之后就是**RRF（倒数秩融合）**：将两路召回的结果合并去重，按公式重新打分排序。

第三步是<span style="background-color: yellow">**重排**</span>：

向量检索的Top-K结果只是“粗排”，可能混入很多表面相似但实际无关的Chunk。引入一个专门的重排模型，将粗排后的 Top-k 个Chunk，与用户的Query进行精细化的交叉注意力计算，重新打分，取出 Top-5。

第四步就可以<span style="background-color: yellow">**上下文组装，然后生成最终的问题**</span>了：

提示词模板的话，强烈建议结构化Prompt，比如下面一个例子：

```txt 
你是一个专业的问答助手。请严格基于以下【参考资料】回答用户问题。
如果资料中没有答案，请直接回答“根据已知信息无法回答”，严禁自己编造。

【参考资料】
{context_variables}

【用户问题】
{query}
```

后面就是<span style="background-color: yellow">**流式输出 + 监控评估**</span>。


## 2. 用户输入部分




## 3. 混合检索




## 4. 重排







## 5. 最终问题

