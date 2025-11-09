import sqlite3
con = sqlite3.connect("data/hosts.db")
cur = con.cursor()

res = cur.execute(f"SELECT * from host")
print(res.fetchall())