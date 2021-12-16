docker system prune -f
docker rmi -f dna-backend:latest
docker rmi -f dna-frontend:latest
docker rmi -f dna-base:frontend
docker rmi -f dna-base:all