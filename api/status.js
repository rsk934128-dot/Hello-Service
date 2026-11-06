module.exports = (req, res) => {
    res.setHeader('Content-Type', 'application/json');
    res.setHeader('Access-Control-Allow-Origin', '*');
    res.status(200).json({
        service: 'NoorLink Cloud Gateway (Vercel)',
        version: '1.0.0',
        pwa: true,
        jwtSecurity: 'Active',
        timestamp: Date.now()
    });
};
