#!/bin/bash
set -e

mkdir -p bundle/tmp
cp -r policies/* bundle/tmp/

# Add manifest
cat > bundle/tmp/.manifest <<EOF
{
  "revision": "v1"
}
EOF

tar -czf bundle/bundle.tar.gz -C bundle/tmp .
rm -rf bundle/tmp
