import sqlite3


def must_contain(words: list):
    filter = ""
    for word in words:
        if word == "":
            continue
        filter = filter + f" AND text LIKE '%,{word.lower()},%'"
    length = len(" AND ")
    filter = filter[length:]
    return filter


def print_results(results):
    for result in results:
        print(f"{result[0]}")
    print(f"{len(results)} results")


def must_contain_any(words: list):
    filter = ""
    for word in words:
        if word == "":
            continue
        filter = filter + f" OR text LIKE '%,{word.lower()},%'"
    length = len(" OR ")
    filter = filter[length:]
    return filter


def must_not_contain_all(words: list):
    filter = ""
    for word in words:
        if word == "":
            continue
        filter = filter + f" AND text NOT LIKE '%,{word.lower()},%'"
    length = len(" AND ")
    filter = filter[length:]
    return filter


def do_filter(cur, or_word_list, and_word_list, not_words):
    and_list = must_contain(and_word_list)
    or_list = must_contain_any(or_word_list)
    not_list = must_not_contain_all(not_words)
    filters = []
    if and_list != "":
        filters.append(and_list)
    if or_list != "":
        filters.append(or_list)
    if not_list != "":
        filters.append(not_list)

    filter = " AND ".join(filters)
    if filter == "":
        query = "SELECT URL from site"
    else:
        query = f"SELECT URL from site where {filter}"
    res = cur.execute(query)
    results = res.fetchall()
    return results


def search_on_host(or_list, and_list, not_list):
    con = sqlite3.connect("data/websites.db")
    cur = con.cursor()

    return do_filter(cur, or_list, and_list, not_list)
