import sqlite3
con = sqlite3.connect("data/lite.cnn.com.db")
cur = con.cursor()

res = cur.execute(f"SELECT * from site")
print(res.fetchall())