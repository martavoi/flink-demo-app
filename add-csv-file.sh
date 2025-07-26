#!/bin/bash

# Script to create a new CSV file in the input directory

FILE_NUM=$(date +%s)  # Use timestamp as file number
TIMESTAMP=$(date '+%Y-%m-%d %H:%M:%S')
FILENAME="input/data_${FILE_NUM}.csv"

# Create a new CSV file with header and a few records
cat > "$FILENAME" << EOF
profile_id,amount,timestamp
user001,$(shuf -i 50-500 -n 1).$(shuf -i 10-99 -n 1),$TIMESTAMP
user002,$(shuf -i 50-500 -n 1).$(shuf -i 10-99 -n 1),$TIMESTAMP
user003,$(shuf -i 50-500 -n 1).$(shuf -i 10-99 -n 1),$TIMESTAMP
EOF

echo "Created new CSV file: $FILENAME"
echo "Content:"
cat "$FILENAME"
echo ""
echo "Monitor job progress at: http://localhost:8081" 