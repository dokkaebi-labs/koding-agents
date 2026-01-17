package theunderdog.ai.tools

import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import theunderdog.ai.utills.resolveFilePath

@Tool("editFile")
@LLMDescription("""
    텍스트 교체를 통해 파일을 생성하거나 수정합니다
    - 파일이 없고 old_str이 비어있으면: new_str로 새 파일 생성
    - 파일이 있고 old_str이 비어있으면: 파일 끝에 new_str 추가
    - old_str이 비어있지 않으면: old_str을 new_str로 교체
      (정확히 1회만 매칭되어야 함)
""")
fun editFile(
    @LLMDescription("수정하거나 생성할 파일 경로")
    path: String,
    @LLMDescription("찾아서 교체할 문자열 (빈 문자열 = 생성/추가 모드)")
    oldStr: String,
    @LLMDescription("교체할 새 문자열")
    newStr: String,
): String {
    if (oldStr == newStr) return "오류: 두 문자열이 동일합니다."
    val file = resolveFilePath(path)

    if (!file.exists()) {
        return if (oldStr.isEmpty()) {
            try {
                file.parentFile?.mkdirs()
                file.writeText(newStr, Charsets.UTF_8)
                "성공: newStr로 '$path'에 새 파일을 생성했습니다."
            } catch (e: Exception) {
                """오류: 파일 생성을 실패하였습니다. > ${e.message}"""
            }
        } else {
            "오류: 수정 할 파일을 찾을 수 없습니다."
        }
    }

    val content = try {
        file.readText(Charsets.UTF_8)
    } catch (e: Exception) {
        return "오류: 파일 읽기를 실패하였습니다. > ${e.message}"
    }

    if (oldStr.isEmpty()) {
        // Append Mode
        try {
            file.appendText(newStr, Charsets.UTF_8)
            return "성공: 기존 파일에 새 내용을 추가했습니다.: $path"
        } catch (e: Exception) {
            "오류: 추가하는 데 실패했습니다: ${e.message}"
        }
    }

    // Replace Mode
    val count = content.split(oldStr).size - 1
    if (count == 0) return "오류: 수정할 문자열을 찾을 수 없습니다."
    if (count > 1) return "확인필요: old_str이 여러 번 발견됨($count 발생)"
    val newContent = content.replace(oldStr, newStr)

    return try {
        file.writeText(newContent, Charsets.UTF_8)
        "성공: 파일 '$path'의 내용이 성공적으로 교체되었습니다."
    } catch (e: Exception) {
        "오류: 콘텐츠를 쓰는데 실패했습니다: ${e.message}"
    }
}