cd lsr
make
for /l %%x in (1, 1, 5) do (
  for /l %%y in (1, 1, 5) do (
    java Project3 2 %%y ../top%%x.txt > ../results/top%%x/lsr%%y.txt
  )
)

cd ../dvr
make
for /l %%x in (1, 1, 5) do (
  for /l %%y in (1, 1, 5) do (
    java Project3 2 %%y ../top%%x.txt > ../results/top%%x/dvr%%y.txt
  )
)
cd ..