FROM node:15-alpine

# Create app directory
WORKDIR /usr/src/app

RUN mkdir -p /usr/src/app/user_datasets
RUN mkdir -p /usr/src/app/misc

# Install app dependencies
# A wildcard is used to ensure both package.json AND package-lock.json are copied
# where available (npm@5+)
COPY package*.json ./

RUN npm install
# If you are building your code for production
# RUN npm ci --only=production

# Bundle app source
COPY . .

EXPOSE 4560
CMD [ "node", "datatransfer.js" ]