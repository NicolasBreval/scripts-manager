import sys

with open('params-test.txt', 'w+') as file:
    file.write(','.join(sys.argv))