import sys

filename = sys.argv[1]
f = open(filename, "r")
summary = open("summary.txt", "w")

ok_total = 0
parsing_timeout_total = 0
parsing_out_of_memory_total = 0
parsing_stack_overflow_total = 0
parsing_other_exception_total = 0
termex_timeout_total = 0
delta_table_computation_timeout_total = 0
grammar_finding_timeout_total = 0
sol_timeout_total = 0
prcons_timeout_total = 0
cutintro_out_of_memory_total = 0
cutintro_stack_overflow_total = 0
cutintro_uncompressible_total = 0
cutintro_ehs_unprovable_total = 0
cutintro_other_exception_total = 0

first = True 
 
for line in f:
  if line.startswith("----------"):
    if not first:
      summary.write(title)
      summary.write("ok: " + str(ok) + "\n")
      summary.write("parsing_timeout: " + str(parsing_timeout) + "\n")
      summary.write("parsing_out_of_memory: " + str(parsing_out_of_memory) + "\n")
      summary.write("parsing_stack_overflow: " + str(parsing_stack_overflow) + "\n")
      summary.write("parsing_other_exception: " + str(parsing_other_exception) + "\n")
      summary.write("termex_timeout: " + str(termex_timeout) + "\n")
      summary.write("delta_table_computation_timeout: " + str(delta_table_computation_timeout) + "\n")
      summary.write("grammar_finding_timeout: " + str(grammar_finding_timeout) + "\n")
      summary.write("sol_timeout: " + str(sol_timeout) + "\n")
      summary.write("prcons_timeout: " + str(prcons_timeout) + "\n")
      summary.write("cutintro_out_of_memory: " + str(cutintro_out_of_memory) + "\n")
      summary.write("cutintro_stack_overflow: " + str(cutintro_stack_overflow) + "\n")
      summary.write("cutintro_uncompressible: " + str(cutintro_uncompressible) + "\n")
      summary.write("cutintro_ehs_unprovable: " + str(cutintro_ehs_unprovable) + "\n")
      summary.write("cutintro_other_exception: " + str(cutintro_other_exception) + "\n\n")
    else:
      first = False
    ok = 0
    parsing_timeout = 0
    parsing_out_of_memory = 0
    parsing_stack_overflow = 0
    parsing_other_exception = 0
    termex_timeout = 0
    delta_table_computation_timeout = 0
    grammar_finding_timeout = 0
    sol_timeout = 0
    prcons_timeout = 0
    cutintro_out_of_memory = 0
    cutintro_stack_overflow = 0
    cutintro_uncompressible = 0
    cutintro_ehs_unprovable = 0
    cutintro_other_exception = 0
    title = line
  else:
    info = line.split(",")
    status = info[1].strip()
    if status == "ok":
      ok += 1
      ok_total += 1
    elif status == "parsing_timeout":
      parsing_timeout += 1
      parsing_timeout_total += 1
    elif status == "parsing_out_of_memory":
      parsing_out_of_memory += 1
      parsing_out_of_memory_total += 1
    elif status == "parsing_stack_overflow":
      parsing_stack_overflow +=1
      parsing_stack_overflow_total +=1
    elif status == "parsing_other_exception":
      parsing_other_exception += 1
      parsing_other_exception_total += 1
    elif status == "termex_timeout":
      termex_timeout += 1
      termex_timeout_total += 1
    elif status == "delta_table_computation_timeout":
      delta_table_computation_timeout += 1
      delta_table_computation_timeout_total += 1
    elif status == "grammar_finding_timeout":
      grammar_finding_timeout += 1
      grammar_finding_timeout_total += 1
    elif status == "sol_timeout":
      sol_timeout += 1
      sol_timeout_total += 1
    elif status == "prcons_timeout":
      prcons_timeout += 1
      prcons_timeout_total += 1
    elif status == "cutintro_out_of_memory":
      cutintro_out_of_memory += 1
      cutintro_out_of_memory_total += 1
    elif status == "cutintro_stack_overflow":
      cutintro_stack_overflow += 1
      cutintro_stack_overflow_total += 1
    elif status == "cutintro_uncompressible":
      cutintro_uncompressible += 1
      cutintro_uncompressible_total += 1
    elif status == "cutintro_ehs_unprovable":
      cutintro_ehs_unprovable += 1
      cutintro_ehs_unprovable_total += 1
    elif status == "cutintro_other_exception":
      cutintro_other_exception += 1
      cutintro_other_exception_total += 1
    else:
      print "Unrecognized status: " + status

# Last one
summary.write(title)
summary.write("ok: " + str(ok) + "\n")
summary.write("parsing_timeout: " + str(parsing_timeout) + "\n")
summary.write("parsing_out_of_memory: " + str(parsing_out_of_memory) + "\n")
summary.write("parsing_stack_overflow: " + str(parsing_stack_overflow) + "\n")
summary.write("parsing_other_exception: " + str(parsing_other_exception) + "\n")
summary.write("termex_timeout: " + str(termex_timeout) + "\n")
summary.write("delta_table_computation_timeout: " + str(delta_table_computation_timeout) + "\n")
summary.write("grammar_finding_timeout: " + str(grammar_finding_timeout) + "\n")
summary.write("sol_timeout: " + str(sol_timeout) + "\n")
summary.write("prcons_timeout: " + str(prcons_timeout) + "\n")
summary.write("cutintro_out_of_memory: " + str(cutintro_out_of_memory) + "\n")
summary.write("cutintro_stack_overflow: " + str(cutintro_stack_overflow) + "\n")
summary.write("cutintro_uncompressible: " + str(cutintro_uncompressible) + "\n")
summary.write("cutintro_ehs_unprovable: " + str(cutintro_ehs_unprovable) + "\n")
summary.write("cutintro_other_exception: " + str(cutintro_other_exception) + "\n\n")

summary.write("---------- TOTALS\n")
summary.write("ok: " + str(ok_total) + "\n")
summary.write("parsing_timeout: " + str(parsing_timeout_total) + "\n")
summary.write("parsing_out_of_memory: " + str(parsing_out_of_memory_total) + "\n")
summary.write("parsing_stack_overflow: " + str(parsing_stack_overflow_total) + "\n")
summary.write("parsing_other_exception: " + str(parsing_other_exception_total) + "\n")
summary.write("termex_timeout: " + str(termex_timeout_total) + "\n")
summary.write("delta_table_computation_timeout: " + str(delta_table_computation_timeout_total) + "\n")
summary.write("grammar_finding_timeout: " + str(grammar_finding_timeout_total) + "\n")
summary.write("sol_timeout: " + str(sol_timeout_total) + "\n")
summary.write("prcons_timeout: " + str(prcons_timeout_total) + "\n")
summary.write("cutintro_out_of_memory: " + str(cutintro_out_of_memory_total) + "\n")
summary.write("cutintro_stack_overflow: " + str(cutintro_stack_overflow_total) + "\n")
summary.write("cutintro_uncompressible: " + str(cutintro_uncompressible_total) + "\n")
summary.write("cutintro_ehs_unprovable: " + str(cutintro_ehs_unprovable_total) + "\n")
summary.write("cutintro_other_exception: " + str(cutintro_other_exception_total) + "\n\n")
