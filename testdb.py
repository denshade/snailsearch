import sqlite3
con = sqlite3.connect("data/nl.wikipedia.org.db")
cur = con.cursor()

res = cur.execute(f"SELECT * from site")
print(res.fetchall())