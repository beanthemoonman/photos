# Photos
![Build Status](https://github.com/beanthemoonman/photos/actions/workflows/build.yml/badge.svg)

A dumb little web app that lets me show photos easily to friends and family.

Made with JetBrains Junie as kind of a demented way for me to be able to speak more to where and how agentic AI might be
useful for programmers.

## Docker Support

This application can be run in Docker. To build and run the application using Docker:

### Build the Docker image

```bash
docker build -t photos-app .
```

### Run the Docker container

```bash
docker run -p 8080:8080 -v /path/to/your/photos:/app/photos photos-app
```

Replace `/path/to/your/photos` with the path to your photos directory on the host machine.

### Access the application

Once the container is running, you can access the application at http://localhost:8080
