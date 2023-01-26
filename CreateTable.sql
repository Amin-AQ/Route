
-- uncomment below line if db does not exist
--CREATE DATABASE logs

use logs

CREATE TABLE [User](
	PhoneNumber VARCHAR(20) NOT NULL,
	[Password] VARCHAR(255) NOT NULL
	PRIMARY KEY(PhoneNumber)
)

CREATE TABLE [Log](
	PhoneNumber VARCHAR(20) NOT NULL FOREIGN KEY REFERENCES [User](PhoneNumber),
	[DateStamp] DATE NOT NULL,
	[TimeStamp] TIME NOT NULL,
	Latitude VARCHAR(100) NOT NULL,
	Longitude VARCHAR(100) NOT NULL
	PRIMARY KEY(PhoneNumber, [DateStamp], [TimeStamp])
)