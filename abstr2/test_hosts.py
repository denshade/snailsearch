from unittest import TestCase

from abstr2.context import ContextMap
from abstr2.hosts import load_hosts, init_hosts_index, start_scan_index, get_indices, stop_scan_index


class Test(TestCase):
    def test_start_scan_index(self):
        contextmap = ContextMap()
        load_hosts(contextmap)
        init_hosts_index(contextmap)
        start_scan_index(contextmap, "test")
        indices = get_indices(contextmap)
        filtered = [index for index in indices if index.index_name == "test"]
        self.assertTrue(len(filtered) > 0)
        self.assertIsNotNone(filtered[0].update_start)
        self.assertIsNone(filtered[0].update_end)
        stop_scan_index(contextmap, "test")
        indices = get_indices(contextmap)
        filtered = [index for index in indices if index.index_name == "test"]
        self.assertEqual(filtered[0].pct_complete, 100)
        self.assertIsNotNone(filtered[0].update_end)


