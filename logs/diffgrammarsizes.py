import sys
import pandas as pd

filename = sys.argv[1]
data = pd.read_csv(filename, names=['method', 'file', 'status', 'infer-in', 'qinfer-in', 'infer-out', 'qinfer-out', 'termset', 'mingrammar', 'num_mingrammar', 'sol-can', 'sol-min', 'time-termset', 'time-delta', 'time-grammar', 'time-sol', 'time-prcons', 'time-clean'])

grammar_data = data[['file', 'method', 'mingrammar']].replace(' ', -1).groupby('file')

print "Inconsistent results (different minimal grammar sizes):"
print "TODO: check if the methods are introducing the same number of cuts."

i = 0
for name, info in grammar_data:
  info = [(m,g) for _,m,g in info.values if int(g) >= 0]
  if len(info) > 0:
    grammarsize = info[0][1]
    if not all(g == grammarsize for _,g in info):
      i = i + 1
      print name
      for method, gsize in info:
	print method, " ", gsize

print "Number of inconsistent results: ", i


