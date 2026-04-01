package com.feng.rag.datasource.util;

import java.util.regex.Pattern;

/**
 * 文本清洗工具类-------【对应清洗步骤一：确定规则清洗，文档内容解析后清洗一遍】
 */
public final class TextCleanupUtils {

    // 预编译正则，避免每次调用重新编译（性能优化）
    /** 匹配连续 3 个以上的换行符 */
    private static final Pattern EXCESSIVE_NEWLINES = Pattern.compile("\n{3,}");
    /** 匹配连续 3 个以上的空格（保留段落缩进的合理空格） */
    private static final Pattern EXCESSIVE_SPACES = Pattern.compile(" {3,}");
    /** 匹配控制字符（\x00-\x08 \x0b \x0c \x0e-\x1f \x7f），保留 \t \n \r */
    private static final Pattern CONTROL_CHARS = Pattern.compile("[\u0000-\u0008\u000B\u000C\u000E-\u001F\u007F]");
    /** 匹配 Windows 风格行尾（\r\n → \n） */
    private static final Pattern WINDOWS_NEWLINE = Pattern.compile("\r\n");
    /** 匹配孤立的 \r（老 Mac 风格） */
    private static final Pattern CR_ONLY = Pattern.compile("\r");
    /** 匹配行尾空白（每行末尾的空格/制表符） */
    private static final Pattern TRAILING_SPACES = Pattern.compile("[ \t]+\n");
    /** 匹配 Unicode 零宽字符（零宽连接符、零宽非连接符、零宽空格） */
    private static final Pattern ZERO_WIDTH_CHARS = Pattern.compile("[\u200B-\u200D\uFEFF]");
    /** 匹配重复的分隔线（如 --------、========、========= 等） */
    private static final Pattern SEPARATOR_LINES = Pattern.compile("[-=_*]{10,}\n?");

    // 禁止实例化
    private TextCleanupUtils() {}

    /**
     * 执行完整的文本清洗流程。
     *
     * <p>清洗步骤（有顺序依赖，不可随意调换）：
     * <ol>
     *   <li>去除控制字符（可能导致后续正则行为异常）</li>
     *   <li>统一换行符（Windows \r\n → \n）</li>
     *   <li>去除行尾空白</li>
     *   <li>去除零宽字符（Word 文档中常见）</li>
     *   <li>压缩连续空格</li>
     *   <li>压缩连续空行（最多保留 2 个连续空行）</li>
     *   <li>去除分隔线（减少对 Chunking 的干扰）</li>
     *   <li>去除首尾空白</li>
     * </ol>
     */
    public static String clean(String rawText) {
        if (rawText == null || rawText.isBlank()) return "";

        String text = rawText;
        // 1. 去除控制字符（保留制表符\t、换行符\n、回车\r）
        text = CONTROL_CHARS.matcher(text).replaceAll("");
        // 2. 统一换行符
        text = WINDOWS_NEWLINE.matcher(text).replaceAll("\n");
        text = CR_ONLY.matcher(text).replaceAll("\n");
        // 3. 去除行尾多余空白
        text = TRAILING_SPACES.matcher(text).replaceAll("\n");
        // 4. 去除零宽字符（这类字符在 Word/PPT 中很常见，肉眼不可见但会干扰分词）
        text = ZERO_WIDTH_CHARS.matcher(text).replaceAll("");
        // 5. 压缩连续空格（3个以上→2个，保留代码缩进的合理空格）
        text = EXCESSIVE_SPACES.matcher(text).replaceAll("  ");
        // 6. 压缩连续空行（3行以上→2行，保留段落间距）
        text = EXCESSIVE_NEWLINES.matcher(text).replaceAll("\n\n");
        // 7. 去除明显的装饰性分隔线（减少噪声）
        text = SEPARATOR_LINES.matcher(text).replaceAll("");
        // 8. 去除首尾空白
        return text.strip();
    }

    /**
     * 仅做最轻量的清洗（换行统一 + 首尾去空）。
     */
    public static String lightClean(String rawText) {
        if (rawText == null || rawText.isBlank()) return "";

        String text = WINDOWS_NEWLINE.matcher(rawText).replaceAll("\n");
        text = CR_ONLY.matcher(text).replaceAll("\n");
        return text.strip();
    }

    /**
     * 计算文本的有效字符比例（有效字符 / 总字符）。
     * <p>比例低于 0.5 通常说明文本质量差（乱码、扫描识别错误等）。
     * 可用于质量过滤前的快速评估。
     */
    public static double calcValidCharRatio(String text) {
        if (text == null || text.isEmpty()) {
            return 0.0;
        }
        long validCount = text.chars()
                .filter(c ->
                        // 中文字符（基本汉字区）
                        (c >= 0x4E00 && c <= 0x9FFF) ||
                                // 英文字母和数字
                                Character.isLetterOrDigit(c) ||
                                // 常用标点（中英文）：修复双引号转义问题
                                "，。！？、；：\"\"''（）【】,.!?;:'\"()[]{}".indexOf(c) >= 0 ||
                                // 空白字符（空格、制表符、换行等）
                                Character.isWhitespace(c)
                )
                .count();
        return (double) validCount / text.length();
    }

    /**
     * 截断文本到指定字符数，在词边界处截断（避免截断汉字中间）。
     */
    public static String truncate(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        // 在最大长度处向前找到最近的换行符（段落边界），在段落处截断
        int cutPoint = maxLength;
        int lastNewline = text.lastIndexOf('\n', maxLength);
        if (lastNewline > maxLength * 0.8) {
            // 如果换行符在 80% 位置之后，在换行处截断，减少语义破坏
            cutPoint = lastNewline;
        }
        return text.substring(0, cutPoint) + "\n...[内容已截断]";
    }
}