class MustContainInDocument:
    def __init__(self, contain_text):
        self.contain_text = contain_text

    def matches(self, document):
        return self.contain_text in document