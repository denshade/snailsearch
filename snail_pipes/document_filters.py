class MustContainInDocument:
    def __init__(self, contain_text):
        self.contain_text = contain_text

    def matches(self, document):
        return self.contain_text in document

    def context(self, document):
        return self.get_context(" ".join(document), self.contain_text)

    def get_context(self, text: str, substring: str, context_len: int = 100) -> str:
        """
        Returns the substring along with `context_len` characters before and after it (if available).
        """
        index = text.find(substring)
        if index == -1:
            return None  # substring not found

        start = max(index - context_len, 0)
        end = min(index + len(substring) + context_len, len(text))
        return text[start:end]

