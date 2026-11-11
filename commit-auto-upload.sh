#!/bin/bash
clear
git add .
echo
echo
echo "Welche Änderungen wurden durchgeführt?"
read -e changes
echo
git commit -m "$changes"
echo
git push origin main --progress
