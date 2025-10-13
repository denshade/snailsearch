class URLFilter:
    def __init__(self, contain_text, must_not_contain):
        self.contain_text = contain_text
        self.must_not_contain = must_not_contain

    def matches(self, url):
        for item in self.must_not_contain:
            if item in url:
                return False
        return self.contain_text in url
