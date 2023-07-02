# This basic Python script is used to perform tests for PythonsScriptRunner class

import pandas as pd

# Example from https://pandas.pydata.org/docs/reference/api/pandas.DataFrame.to_csv.html
df = pd.DataFrame({'name': ['Raphael', 'Donatello'], 'mask': ['red', 'purple'], 'weapon': ['sai', 'bo staff']})
df.to_csv('./ninja-turtles.csv', index=False)