from unittest import TestCase

from abstr2.context import ContextMap
from abstr2.hosts import load_hosts, init_hosts_index, start_scan_index, get_indices


class Test(TestCase):
    def test_start_scan_index(self):
        contextmap = ContextMap()
        load_hosts(contextmap)
        init_hosts_index(contextmap)
        start_scan_index(contextmap, "test")
        indices = get_indices(contextmap)
        assert len(indices) > 0


