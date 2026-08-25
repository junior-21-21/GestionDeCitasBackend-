CREATE DATABASE clinica_veterinaria
ON 
( NAME = clinica_veterinaria_dat,
    FILENAME = 'C:\BasesDeDatos\clinica_veterinaria.mdf',
    SIZE = 10,
    MAXSIZE = 50,
    FILEGROWTH = 5 )
LOG ON
( NAME = clinica_veterinaria_log,
    FILENAME = 'C:\BasesDeDatos\clinica_veterinaria.ldf',
    SIZE = 5MB,
    MAXSIZE = 25MB,
    FILEGROWTH = 5MB );
GO
